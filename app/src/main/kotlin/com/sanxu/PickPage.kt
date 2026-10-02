package com.sanxu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.pick.*
import com.sanxu.domain.filter.PlayType
import com.sanxu.ui.NumberFont

val primaryMethods=listOf(PickMethod.SINGLE,PickMethod.GROUP,PickMethod.DISTINCT,PickMethod.POSITION)
fun methodLabel(method:PickMethod)=when(method){PickMethod.SINGLE->"单选";PickMethod.GROUP->"组选";PickMethod.DISTINCT->"单选单";PickMethod.POSITION->"位选";PickMethod.FILTER->"条件筛选";PickMethod.DAN_TUO->"胆拖"}
fun playLabel(play:PlayType)=when(play){PlayType.STRAIGHT->"单选";PlayType.GROUP3->"组三";PlayType.GROUP6->"组六"}

@Composable fun PickPage(model:WorkspaceViewModel,onTools:()->Unit,onDetails:()->Unit) {
    val state by model.editor.collectAsStateWithLifecycle()
    val draft=state.draft
    val input=draft.input
    val result=remember(input){PickEngine.generate(input)}
    var entry by rememberSaveable(input.method,draft.editingId) { mutableStateOf("") }
    var batch by remember { mutableStateOf(false) }
    var help by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value=draft.issue,onValueChange={value->if(value.length<=7&&value.all { it in '0'..'9' }) model.updateDraft { it.copy(issue=value) }},
                label={Text("目标期号")},placeholder={Text("请输入七位期号")},singleLine=true,modifier=Modifier.weight(1f).testTag("issue_input"),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
            TextButton(onClick={model.newEntry();entry=""},enabled=!state.saving) { Text(if(draft.editingId!=null) "新建记录" else "清空") }
        }
        Row(Modifier.padding(horizontal=12.dp,vertical=8.dp).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            primaryMethods.forEach { method ->
                val active=input.method==method
                Surface(onClick={model.chooseMethod(method);entry=""},modifier=Modifier.weight(1f).height(48.dp).testTag("method_${method.name}"),shape=RoundedCornerShape(12.dp),
                    color=if(active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor=if(active) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                    enabled=state.ready&&!state.saving) { Box(contentAlignment=Alignment.Center) { Text(methodLabel(method),fontSize=14.sp,fontWeight=if(active) FontWeight.Bold else FontWeight.Normal) } }
            }
        }
        if(!state.ready) { Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){CircularProgressIndicator()} }
        else Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            when(input.method) {
                PickMethod.SINGLE -> {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        SmallLabel("依次点选百位、十位、个位",Modifier.weight(1f))
                        TextButton(onClick={batch=true}) { Text("批量录入") }
                    }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                        repeat(3) { index -> Column(Modifier.width(72.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(entry.getOrNull(index)?.toString() ?: "—",fontSize=40.sp,fontFamily=NumberFont,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.primary)
                            SmallLabel(listOf("百位","十位","个位")[index])
                        } }
                        IconButton(onClick={entry=entry.dropLast(1)}) { Icon(Icons.Outlined.Backspace,"退一位") }
                    }
                    DigitGrid(emptySet(),"single") { digit ->
                        entry=(if(entry.length==3) "" else entry)+digit
                        if(entry.length==3) model.updateInput { it.copy(manual=(it.manual+entry).distinct(),excluded=emptySet()) }
                    }
                    if(input.manual.isNotEmpty()) Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        input.manual.forEach { number -> InputChip(selected=true,onClick={model.updateInput { it.copy(manual=it.manual-number) }},label={Text(number,fontFamily=NumberFont)},trailingIcon={Icon(Icons.Outlined.Close,"移除 $number",Modifier.size(14.dp))}) }
                    }
                }
                PickMethod.GROUP -> {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf(PlayType.GROUP3,PlayType.GROUP6).forEach { play -> FilterChip(selected=input.play==play,onClick={model.updateInput { it.copy(play=play,manual=emptyList(),excluded=emptySet()) }},label={Text(playLabel(play))},modifier=Modifier.testTag("group_${play.name}")) }
                        Spacer(Modifier.weight(1f));TextButton(onClick={batch=true}){Text("直接录入")}
                    }
                    Text(if(input.play==PlayType.GROUP3) "两位相同，一位不同" else "三位数字各不相同",style=MaterialTheme.typography.titleLarge)
                    SmallLabel(if(input.play==PlayType.GROUP3) "选 0、1 → 001、011；不分顺序" else "选 0、1、2 → 012；不分顺序")
                    if(input.byNumber) {
                        Text(input.manual.joinToString("   "),fontFamily=NumberFont)
                        TextButton(onClick={model.updateInput { it.copy(byNumber=false,manual=emptyList()) }}){Text("改用数字池")}
                    } else { DigitGrid(input.digits,"group"){digit-> model.updateInput { it.copy(digits=it.digits.toggle(digit),excluded=emptySet()) } }; PoolTools(input.digits){digits->model.updateInput { it.copy(digits=digits,excluded=emptySet()) }} }
                }
                PickMethod.DISTINCT -> {
                    Text("不同数字，按顺序排列",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(top=10.dp))
                    SmallLabel("至少选 3 个数字。选 0、1、2，共生成 6 注单选。")
                    Spacer(Modifier.height(8.dp))
                    DigitGrid(input.digits,"distinct"){digit->model.updateInput { it.copy(digits=it.digits.toggle(digit),excluded=emptySet()) }}
                    PoolTools(input.digits){digits->model.updateInput { it.copy(digits=digits,excluded=emptySet()) }}
                    Text("012   021   102   120   201   210",fontFamily=NumberFont,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    SmallLabel("示例说明 · 实际号码随你的选择更新")
                }
                PickMethod.POSITION -> {
                    repeat(3) { position ->
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.width(34.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(listOf("百","十","个")[position],fontWeight=FontWeight.Bold)
                                SmallLabel("${input.positions[position].size}选")
                            }
                            DigitGrid(input.positions[position],"pos$position",Modifier.weight(1f)){digit-> model.updateInput { current->current.copy(positions=current.positions.mapIndexed { i,set-> if(i==position) set.toggle(digit) else set },excluded=emptySet()) } }
                        }
                    }
                }
                PickMethod.FILTER,PickMethod.DAN_TUO -> {
                    Text(methodLabel(input.method),style=MaterialTheme.typography.titleLarge)
                    SmallLabel("工具已生成 ${result.numbers.size} 个号码，可以检查明细后加入方案。")
                    OutlinedButton(onClick=onTools){Text("修改工具条件")}
                }
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick=onDetails,enabled=result.numbers.isNotEmpty()||input.excluded.isNotEmpty()) { Text("查看 ${result.numbers.size} 个号码"); Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp)) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick={help=true}) { Icon(Icons.Outlined.HelpOutline,"玩法说明") }
            }
            if(result.problem!=null) SmallLabel(when(result.problem){PickProblem.INCOMPLETE->when(input.method){PickMethod.POSITION->"百位、十位、个位都至少选一个数字";PickMethod.SINGLE->"组成三位号码后会自动加入上方列表";else->"继续选数，完成后会自动计算注数"};PickProblem.NO_RESULTS->"没有符合条件的号码，请调整筛选";PickProblem.INVALID_SHAPE->"号码不符合当前组三或组六类型";else->"请检查号码或筛选条件"})
        }
        Surface(shadowElevation=3.dp,color=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal=16.dp,vertical=8.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${result.numbers.size} 个号码 · ${result.numbers.size*draft.multiplier} 注",style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("pick_count"))
                        Text("¥ ${result.numbers.size*draft.multiplier*2}",fontSize=25.sp,fontFamily=NumberFont,fontWeight=FontWeight.Bold,modifier=Modifier.testTag("pick_amount"))
                    }
                    IconButton(onClick={model.updateDraft { it.copy(multiplier=(it.multiplier-1).coerceAtLeast(1)) }},enabled=draft.multiplier>1){Icon(Icons.Outlined.Remove,"减少倍数")}
                    Text("${draft.multiplier} 倍",fontWeight=FontWeight.Bold)
                    IconButton(onClick={model.updateDraft { it.copy(multiplier=(it.multiplier+1).coerceAtMost(99)) }},enabled=draft.multiplier<99){Icon(Icons.Outlined.Add,"增加倍数")}
                }
                Button(onClick=model::save,modifier=Modifier.fillMaxWidth().height(48.dp).testTag("save_plan"),shape=RoundedCornerShape(12.dp),enabled=state.ready&&!state.saving&&result.problem==null&&draft.issue.length==7) {
                    if(state.saving) CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp,color=MaterialTheme.colorScheme.onPrimary)
                    else { Icon(Icons.Outlined.Add,null,Modifier.size(18.dp)); Spacer(Modifier.width(8.dp));Text(if(draft.editingId==null) "加入本期方案" else "更新这条记录") }
                }
            }
        }
    }
    if(batch) {
        var text by remember { mutableStateOf("") }
        var invalid by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest={batch=false},title={Text("批量录入${if(input.method==PickMethod.GROUP) playLabel(input.play) else "单选"}")},text={Column {
            Text("每个号码保留三位，用空格、逗号或换行分隔。")
            OutlinedTextField(value=text,onValueChange={text=it;invalid=false},placeholder={Text("007 012 268")},isError=invalid,modifier=Modifier.testTag("batch_input"))
            if(invalid) Text("请检查三位号码和组选类型",color=MaterialTheme.colorScheme.error)
        }},confirmButton={TextButton(onClick={
            val numbers=text.trim().split(Regex("[\\s,，;；]+"))
            val updated=input.copy(manual=(input.manual+numbers).distinct(),byNumber=input.method==PickMethod.GROUP,excluded=emptySet())
            if(PickEngine.generate(updated).problem==null){model.updateInput { updated };batch=false}else invalid=true
        }){Text("加入号码")}},dismissButton={TextButton(onClick={batch=false}){Text("取消")}})
    }
    if(help) AlertDialog(onDismissRequest={help=false},title={Text("四种选号方式")},text={Text("单选：三位数字与顺序完全一致。\n\n组选：组三为两同一异，组六为三位各异，不分顺序。\n\n单选单：从数字池中取三个不同数字，列出全部顺序，按单选核对。\n\n位选：分别选百位、十位、个位，生成全部组合，按单选核对。\n\n每注 2 元，金额仅用于整理个人方案。")},confirmButton={TextButton(onClick={help=false}){Text("知道了")}})
}

fun Set<Int>.toggle(digit:Int)=if(digit in this) this-digit else this+digit
@Composable fun DigitGrid(selected:Set<Int>,tag:String,modifier:Modifier=Modifier,onDigit:(Int)->Unit) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {
        repeat(2){row-> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            repeat(5){column-> val digit=row*5+column;val active=digit in selected
                Surface(onClick={onDigit(digit)},modifier=Modifier.weight(1f).height(48.dp).testTag("${tag}_$digit").semantics{this.selected=active;contentDescription="$digit"},shape=RoundedCornerShape(10.dp),
                    color=if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor=if(active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border=if(active) null else BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
                    Box(contentAlignment=Alignment.Center){Text(digit.toString(),fontFamily=NumberFont,fontWeight=if(active) FontWeight.Bold else FontWeight.Medium,fontSize=22.sp)}
                }
            }
        } }
    }
}
@Composable fun PoolTools(selected:Set<Int>,onChange:(Set<Int>)->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        SmallLabel("已选 ${selected.size} 个数字",Modifier.weight(1f))
        TextButton(onClick={onChange((0..9).toSet())}){Text("全选")}
        TextButton(onClick={onChange((0..9).toSet()-selected)}){Text("反选")}
        TextButton(onClick={onChange(emptySet())}){Text("清空")}
    }
}
