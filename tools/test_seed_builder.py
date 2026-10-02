import importlib.util,unittest,tempfile,json,sqlite3
from contextlib import closing
from pathlib import Path
spec=importlib.util.spec_from_file_location('builder',Path(__file__).with_name('build-prepackaged-database.py'))
builder=importlib.util.module_from_spec(spec);spec.loader.exec_module(builder)

class SeedBuilderTest(unittest.TestCase):
    def records(self):
        return [dict(issue=f'201700{i}',drawDate=f'2017-01-0{i}',digits=[0,0,i],officialDetailUrl='https://www.cwl.gov.cn/') for i in (1,2)]
    def test_accepts_valid_declared_range_without_hard_coded_end(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);records=self.records()
            source=root/'seed.json';target=root/'sanxu.db'
            source.write_text(json.dumps(dict(draws=records,range=dict(firstIssue='2017001',lastIssue='2017002',count=2))))
            builder.build_database(source,builder.DEFAULT_SCHEMA,target)
            with closing(sqlite3.connect(target)) as db:
                self.assertEqual(db.execute('select count(*) from draws').fetchone()[0],2)
                self.assertEqual(db.execute('pragma integrity_check').fetchone()[0],'ok')
    def test_rejects_gap_invalid_date_and_unicode(self):
        for key,value in [('issue','2017003'),('drawDate','2017-02-30'),('issue','２０１７００２')]:
            records=self.records();records[1][key]=value
            with self.assertRaises(ValueError):builder.validate_records(records)
    def test_failure_preserves_previous_database(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);source=root/'seed.json';target=root/'old.db'
            target.write_bytes(b'previous intact')
            source.write_text(json.dumps(dict(draws=self.records(),range=dict(firstIssue='2017001',lastIssue='2017002',count=3))))
            with self.assertRaises(ValueError):builder.build_database(source,builder.DEFAULT_SCHEMA,target)
            self.assertEqual(target.read_bytes(),b'previous intact')

if __name__=='__main__':unittest.main()
