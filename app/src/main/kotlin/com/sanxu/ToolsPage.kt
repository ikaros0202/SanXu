package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.pick.*
import com.sanxu.domain.filter.*
import com.sanxu.domain.repository.*
import com.sanxu.domain.backtest.*
import com.sanxu.domain.attributes.*
import com.sanxu.domain.scheme.FilterTemplate
import com.sanxu.feature.pick.ConditionEditor
import com.sanxu.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun ToolsPage(model:WorkspaceViewModel,onPick:()->Unit,onBacktest:()->Unit) {
    val state by model.editor.collectAsStateWithLifecycle()
    val methods by model.methods.collectAsStateWithLifecycle()
    val input=state.draft.input
    val conditions=remember(input.conditionsJson) { if(input.conditionsJson.isBlank()) emptyList() else ConditionCodec.decode(input.conditionsJson) }
    var editorIndex by remember { mutableStateOf<Int?>(null) }
    var naming by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<SavedMethod?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        PaperPanel(emphasized=true) { Text("当前作用于：${methodLabel(input.method)}",fontWeight=FontWeight.SemiBold);SmallLabel("筛选在当前选号上继续缩小范围，返回后会保留输入。") }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick={model.chooseMethod(PickMethod.FILTER)}){Text("全号码筛选")}
            OutlinedButton(onClick={model.chooseMethod(PickMethod.DAN_TUO)}){Text("胆拖选号")}
        }
        if(input.method==PickMethod.FILTER||input.method==PickMethod.DAN_TUO) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){PlayType.entries.forEach { play->FilterChip(input.play==play,{model.updateInput { it.copy(play=play,excluded=emptySet()) }},label={Text(playLabel(play))}) }}
        }
        if(input.method==PickMethod.DAN_TUO) {
            Text("胆码 · 必须包含",fontWeight=FontWeight.SemiBold)
            DigitGrid(input.dan,"dan") { digit->model.updateInput { it.copy(dan=it.dan.toggle(digit),tuo=it.tuo-digit,excluded=emptySet()) } }
            Text("拖码 · 参与组合",fontWeight=FontWeight.SemiBold)
            DigitGrid(input.tuo,"tuo") { digit->model.updateInput { it.copy(tuo=it.tuo.toggle(digit),dan=it.dan-digit,excluded=emptySet()) } }
            SmallLabel("胆码与拖码互斥。组三最多 1 个胆码，组六最多 2 个。")
        }
        SectionLabel("筛选条件")
        if(conditions.isEmpty()) SmallLabel("尚未添加条件，保留当前全部号码")
        conditions.forEachIndexed { index,condition->
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable{editorIndex=index}.padding(vertical=10.dp)) { Text(condition.title,fontWeight=FontWeight.Medium);SmallLabel(conditionDescription(condition)) }
                IconButton(onClick={model.updateInput { it.copy(conditionsJson=ConditionCodec.encode(conditions.filterIndexed { i,_->i!=index }),excluded=emptySet()) }}){Icon(Icons.Outlined.Close,"移除${condition.title}")}
            };HorizontalDivider()
        }
        OutlinedButton(onClick={editorIndex=-1},modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.Add,null);Text("添加筛选条件")}
        val result=remember(input){PickEngine.generate(input)}
        if(result.problem!=null&&result.problem!=PickProblem.NO_RESULTS) SmallLabel("请完成选号并检查胆码、拖码或筛选条件")
        Button(onClick=onPick,modifier=Modifier.fillMaxWidth()){Text("返回选号 · ${result.numbers.size} 个号码")}
        OutlinedTextField(state.draft.note,{note->if(note.length<=200) model.updateDraft { it.copy(note=note) }},label={Text("这次选号的备注")},supportingText={Text("${state.draft.note.length}/200")},modifier=Modifier.fillMaxWidth())
        SectionLabel("方法模板")
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick={naming=true}){Text("保存当前方法")}
            OutlinedButton(onClick=onBacktest,enabled=result.problem==null){Text("历史回测")}
        }
        if(methods.isEmpty()) SmallLabel("保存后可复用数字池、位置、筛选和排除号码，不包含期号与倍数。")
        methods.forEach { method->
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(method.title,fontWeight=FontWeight.SemiBold);SmallLabel("${methodLabel(method.input.method)} · ${PickEngine.generate(method.input).numbers.size} 个号码") }
                TextButton(onClick={model.applyMethod(method);onPick()}){Text("应用")}
                IconButton(onClick={delete=method}){Icon(Icons.Outlined.DeleteOutline,"删除模板")}
            }
        }
    }
    editorIndex?.let { index -> ConditionEditor(conditions.getOrNull(index),onDismiss={editorIndex=null},onConfirm={condition->
        val updated=conditions.toMutableList().apply { if(index<0) add(condition) else set(index,condition) }
        model.updateInput { it.copy(conditionsJson=ConditionCodec.encode(updated),excluded=emptySet()) };editorIndex=null
    }) }
    if(naming) {
        var title by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest={naming=false},title={Text("保存方法模板")},text={OutlinedTextField(title,{if(it.length<=40)title=it},label={Text("模板名称")},singleLine=true)},confirmButton={TextButton(onClick={model.saveMethod(title,30);if(title.isNotBlank())naming=false}){Text("保存")}},dismissButton={TextButton(onClick={naming=false}){Text("取消")}})
    }
    delete?.let { method->AlertDialog(onDismissRequest={delete=null},title={Text("删除模板？")},text={Text(method.title)},confirmButton={TextButton(onClick={model.deleteMethod(method.id);delete=null}){Text("删除")}},dismissButton={TextButton(onClick={delete=null}){Text("取消")}}) }
}

fun conditionDescription(c:FilterCondition):String {
    fun pos(p:Position)=listOf("百位","十位","个位")[p.ordinal]
    fun nums(v:Set<Int>)=v.sorted().joinToString("、")
    return when(c) {
        is GlobalRequiredDigits->"包含 ${nums(c.digits)}"
        is GlobalExcludedDigits->"排除 ${nums(c.digits)}"
        is PositionAllowed->nums(c.allowedDigits)
        is SumRange->"${c.minimum} 至 ${c.maximum}"
        is SpanRange->"${c.minimum} 至 ${c.maximum}"
        is SumTailAllowed->nums(c.values)
        is OddCountAllowed->"${nums(c.counts)} 个"
        is BigCountAllowed->"${nums(c.counts)} 个"
        is PrimeLikeCountAllowed->"${nums(c.counts)} 个（1、2、3、5、7）"
        is RouteAllowed->"${pos(c.position)}：${nums(c.routes)} 路"
        is GroupShapeCondition->c.values.joinToString("、"){when(it){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"}}
        is ConsecutiveCondition->if(c.requirePair) "包含相邻数字" else "不包含相邻数字"
        is PairRelationRange->"${listOf("百十位","十个位","百个位")[c.position.ordinal]}${if(c.metric==PairMetric.SUM) "之和" else "差值"}：${c.minimum} 至 ${c.maximum}"
        is DanTuoCondition->"胆码 ${nums(c.danDigits)}；拖码 ${nums(c.tuoDigits)}"
    }
}

@Composable fun NumbersPage(model:WorkspaceViewModel) {
    val state by model.editor.collectAsStateWithLifecycle()
    val input=state.draft.input
    val base=remember(input){PickEngine.generate(input.copy(excluded=emptySet())).numbers}
    val selected=remember(input){PickEngine.generate(input).numbers.toSet()}
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) { Text("保留 ${selected.size} / ${base.size} 个",Modifier.weight(1f));TextButton(onClick={model.updateInput { it.copy(excluded=emptySet()) }}){Text("全部恢复")} }
        SmallLabel("点选号码可排除；再次点选恢复。")
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(vertical=12.dp)) {
            items(base.chunked(3)) { row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach { number->FilterChip(selected=number in selected,onClick={model.updateInput { it.copy(excluded=if(number in it.excluded)it.excluded-number else it.excluded+number) }},label={Text(number,fontFamily=NumberFont)},leadingIcon={if(number in selected)Icon(Icons.Outlined.Check,null,Modifier.size(16.dp))},modifier=Modifier.weight(1f).heightIn(min=48.dp)) }
                repeat(3-row.size){Spacer(Modifier.weight(1f))}
            } }
        }
    }
}

@Composable fun BacktestPage(model:WorkspaceViewModel) {
    val draws by model.draws.collectAsStateWithLifecycle()
    val editor by model.editor.collectAsStateWithLifecycle()
    var window by rememberSaveable { mutableIntStateOf(30) }
    var report by remember { mutableStateOf<BacktestReport?>(null) }
    val input=editor.draft.input
    LaunchedEffect(draws,input,window) {
        report=null
        report=withContext(Dispatchers.Default) {
            val candidates=PickEngine.generate(input).numbers.map(DrawNumber::parse)
            // Fixed current selections are replayed against past draws. The generator never reads the target.
            BacktestEngine { _,_,_->candidates }.run(FilterTemplate("current","当前方法",input.effectivePlay,emptyList(),30,1),draws.takeLast(window+30).map { it.asHistoricalDraw() })
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        SmallLabel("将当前方法用于过去的开奖，每期按 1 倍计算。历史表现不代表未来。")
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(30,100,300).forEach { value->FilterChip(window==value,{window=value},label={Text("近 $value 期")}) }}
        val result=report
        if(result==null) LinearProgressIndicator(Modifier.fillMaxWidth())
        else {
            PaperPanel(emphasized=true) {
                Text("${result.coveredCount} / ${result.eligibleCount} 期覆盖",style=MaterialTheme.typography.headlineMedium)
                Text("覆盖率 ${result.coverageRate?.let { "%.1f%%".format(it*100) } ?: "—"}")
                SmallLabel("累计模拟金额 ¥ ${result.cumulativeAmountYuan} · 每期 ${result.averageBetCount?.toInt() ?: 0} 注")
            }
            if(result.eligibleCount==0) EmptyPanel("历史样本不足","需要至少 30 期之前的历史才能开始回测。")
            LazyColumn(Modifier.weight(1f)) { items(result.results.filter { it.status==BacktestStatus.EVALUATED }.asReversed(),key={it.targetIssue}) { row->
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(row.targetIssue,Modifier.weight(1f),fontFamily=NumberFont)
                    Text(if(row.covered==true) "✓ 覆盖" else "— 未覆盖",color=if(row.covered==true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                };HorizontalDivider()
            } }
        }
    }
}

@Composable fun SettingsPage(model:WorkspaceViewModel) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val metadata by model.metadata.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("外观",style=MaterialTheme.typography.titleMedium)
        ThemeChoice.entries.forEach { choice->Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable{model.setTheme(choice)},verticalAlignment=Alignment.CenterVertically) {
            RadioButton(settings.theme==choice,onClick={model.setTheme(choice)})
            Text(when(choice){ThemeChoice.SYSTEM->"跟随系统";ThemeChoice.LIGHT->"纸白";ThemeChoice.DARK->"墨绿"})
        } }
        HorizontalDivider()
        Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)){Text("首页核对提示");SmallLabel("展示已有方案的开奖核对入口")};Switch(settings.showReplayNotice,model::setNotice) }
        Spacer(Modifier.height(12.dp))
        Text("叁序 1.0.0",style=MaterialTheme.typography.titleMedium)
        SmallLabel("福彩3D 数据分析与个人方案整理工具")
        SmallLabel("所有方案保存在这台设备。卸载会删除个人记录。")
        SmallLabel("历史统计用于观察与复盘，不表示未来中奖概率。")
        metadata?.lastSuccessEpochMillis?.let { value->SmallLabel("上次更新：${java.time.Instant.ofEpochMilli(value).atZone(java.time.ZoneId.of("Asia/Shanghai")).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}") }
        OutlinedButton(onClick={model.refresh()}){Text("更新开奖数据")}
    }
}
