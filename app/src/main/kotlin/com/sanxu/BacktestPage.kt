package com.sanxu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.attributes.DrawNumber
import com.sanxu.domain.backtest.*
import com.sanxu.domain.pick.PickEngine
import com.sanxu.domain.scheme.FilterTemplate
import com.sanxu.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun BacktestPage(model:WorkspaceViewModel) {
    val draws by model.draws.collectAsStateWithLifecycle()
    val editor by model.editor.collectAsStateWithLifecycle()
    var window by rememberSaveable{mutableIntStateOf(30)}
    var report by remember{mutableStateOf<BacktestReport?>(null)}
    val input=editor.draft.input
    LaunchedEffect(draws,input,window){
        report=null
        report=withContext(Dispatchers.Default){
            val candidates=PickEngine.generate(input).numbers.map(DrawNumber::parse)
            // Fixed selections: only preceding data is provided to the replay generator.
            BacktestEngine{_,_,_->candidates}.run(FilterTemplate("current","当前方法",input.effectivePlay,emptyList(),30,1),draws.takeLast(window+30).map{it.asHistoricalDraw()})
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        ChoiceRail(listOf("近30期","近100期","近300期"),listOf(30,100,300).indexOf(window),{window=listOf(30,100,300)[it]})
        val result=report
        WorkspacePanel {
            PanelHeading(methodLabel(input.method)){ContextTag("每期1倍",Modifier.padding(end=8.dp))}
            if(result==null)LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp))
            else {
                Row(Modifier.fillMaxWidth().testTag("backtest_ready").padding(horizontal=16.dp,vertical=8.dp)){
                    Metric("${result.coveredCount}/${result.eligibleCount}","覆盖期数",Modifier.weight(1f))
                    Metric(result.coverageRate?.let{"%.1f%%".format(it*100)} ?: "—","覆盖率",Modifier.weight(1f))
                    Metric("¥${result.cumulativeAmountYuan}","模拟金额",Modifier.weight(1f))
                }
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            SmallLabel("固定当前选号回看历史，不代表未来中奖概率。",Modifier.padding(16.dp))
        }
        if(result!=null) {
            if(result.eligibleCount==0)EmptyPanel("历史样本不足","至少需要目标期之前30期历史。")
            else WorkspacePanel(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp)){SmallLabel("期号",Modifier.weight(1f));SmallLabel("当期注数",Modifier.width(80.dp));SmallLabel("核对结果")}
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                LazyColumn(Modifier.fillMaxSize()){items(result.results.filter{it.status==BacktestStatus.EVALUATED}.asReversed(),key={it.targetIssue}){row->
                    Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
                        Text(row.targetIssue,Modifier.weight(1f),fontFamily=NumberFont,fontSize=14.sp)
                        Text(row.candidateCount.toString(),Modifier.width(80.dp),fontFamily=NumberFont,fontSize=14.sp)
                        Text(if(row.covered==true)"✓ 覆盖" else "— 未覆盖",fontSize=12.sp,fontWeight=FontWeight.Medium,color=if(row.covered==true)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    };HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.6f))
                }}
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
