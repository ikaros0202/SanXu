package com.sanxu

import androidx.compose.foundation.BorderStroke
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
import com.sanxu.ui.*

val primaryMethods=listOf(PickMethod.SINGLE,PickMethod.GROUP,PickMethod.DISTINCT,PickMethod.POSITION)
fun methodLabel(method:PickMethod)=when(method){PickMethod.SINGLE->"单选";PickMethod.GROUP->"组选";PickMethod.DISTINCT->"单选单";PickMethod.POSITION->"位选";PickMethod.FILTER->"条件筛选";PickMethod.DAN_TUO->"胆拖"}
fun playLabel(play:PlayType)=when(play){PlayType.STRAIGHT->"单选";PlayType.GROUP3->"组三";PlayType.GROUP6->"组六"}

@Composable fun PickPage(model:WorkspaceViewModel,onTools:()->Unit,onDetails:()->Unit) {
    val state by model.editor.collectAsStateWithLifecycle()
    val draft=state.draft
    val input=draft.input
    val result=remember(input){PickEngine.generate(input)}
    var entry by rememberSaveable(input.method,draft.editingId){mutableStateOf("")}
    var batch by remember { mutableStateOf(false) }
    var help by remember { mutableStateOf(false) }
    var issueEditor by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={issueEditor=true},modifier=Modifier.testTag("issue_picker"),contentPadding=PaddingValues(horizontal=4.dp)) {
                Text(if(draft.issue.isBlank()) "选择目标期号" else "第 ${draft.issue} 期",fontFamily=NumberFont,fontWeight=FontWeight.Medium)
                Icon(Icons.Outlined.ExpandMore,null,Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(1f))
            ContextTag(if(draft.editingId==null) "草稿" else "编辑中")
            TextButton(onClick={model.newEntry();entry=""},enabled=!state.saving) { Text(if(draft.editingId==null) "清空" else "新建") }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=16.dp)) {
            WorkspacePanel {
                ChoiceRail(primaryMethods.map(::methodLabel),primaryMethods.indexOf(input.method),{model.chooseMethod(primaryMethods[it]);entry=""},
                    modifier=Modifier.fillMaxWidth().padding(4.dp),tags=primaryMethods.map{"method_${it.name}"},enabled=state.ready&&!state.saving)
                if(!state.ready) Box(Modifier.fillMaxWidth().height(260.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()}
                else Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    when(input.method) {
                        PickMethod.SINGLE -> {
                            Row(Modifier.fillMaxWidth().height(48.dp),verticalAlignment=Alignment.CenterVertically) {
                                SmallLabel("按百、十、个位依次输入",Modifier.weight(1f))
                                TextButton(onClick={batch=true},contentPadding=PaddingValues(horizontal=8.dp)){Text("批量录入")}
                            }
                            Surface(color=MaterialTheme.colorScheme.surfaceContainerLow,shape=RoundedCornerShape(10.dp)) {
                                Row(Modifier.fillMaxWidth().padding(start=12.dp,top=8.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
                                    repeat(3){index->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
                                        Text(entry.getOrNull(index)?.toString() ?: "—",fontSize=32.sp,fontFamily=NumberFont,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.primary)
                                        SmallLabel(listOf("百位","十位","个位")[index])
                                    }}
                                    IconButton(onClick={entry=entry.dropLast(1)}){Icon(Icons.Outlined.Backspace,"退一位",Modifier.size(20.dp))}
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            DigitGrid(emptySet(),"single") { digit->
                                entry=(if(entry.length==3) "" else entry)+digit
                                if(entry.length==3) model.updateInput { it.copy(manual=(it.manual+entry).distinct(),excluded=emptySet()) }
                            }
                            if(input.manual.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top=4.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                input.manual.forEach { number->InputChip(selected=true,onClick={model.updateInput{it.copy(manual=it.manual-number)}},
                                    label={Text(number,fontFamily=NumberFont)},trailingIcon={Icon(Icons.Outlined.Close,"移除 $number",Modifier.size(14.dp))}) }
                            } else SmallLabel("输满三位自动收录，可连续输入多注。",Modifier.padding(vertical=10.dp))
                        }
                        PickMethod.GROUP -> {
                            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                                ChoiceRail(listOf("组三","组六"),if(input.play==PlayType.GROUP3)0 else 1,{index->model.updateInput{it.copy(play=if(index==0)PlayType.GROUP3 else PlayType.GROUP6,manual=emptyList(),excluded=emptySet())}},
                                    Modifier.weight(1f),tags=listOf("group_GROUP3","group_GROUP6"))
                                TextButton(onClick={batch=true}){Text("直接录入")}
                            }
                            if(input.byNumber) {
                                Row(Modifier.fillMaxWidth().height(48.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Text("逐注号码",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
                                    TextButton(onClick={model.updateInput{it.copy(byNumber=false,manual=emptyList())}}){Text("改用数字池")}
                                }
                                Text(input.manual.joinToString("   "),fontFamily=NumberFont,modifier=Modifier.padding(vertical=8.dp))
                            } else {
                                PoolTools(input.digits){digits->model.updateInput{it.copy(digits=digits,excluded=emptySet())}}
                                DigitGrid(input.digits,"group"){digit->model.updateInput{it.copy(digits=it.digits.toggle(digit),excluded=emptySet())}}
                            }
                            SmallLabel(if(input.play==PlayType.GROUP3) "两同一异 · 选 0、1 得 001、011，不分顺序" else "三位不同 · 选 0、1、2 得 012，不分顺序",Modifier.padding(vertical=10.dp))
                        }
                        PickMethod.DISTINCT -> {
                            PoolTools(input.digits){digits->model.updateInput{it.copy(digits=digits,excluded=emptySet())}}
                            DigitGrid(input.digits,"distinct"){digit->model.updateInput{it.copy(digits=it.digits.toggle(digit),excluded=emptySet())}}
                            SmallLabel("至少选 3 个不同数字，自动展开全部顺序。",Modifier.padding(vertical=10.dp))
                        }
                        PickMethod.POSITION -> {
                            repeat(3) { position->
                                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                    Column(Modifier.width(28.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                        Text(listOf("百","十","个")[position],fontWeight=FontWeight.SemiBold)
                                        SmallLabel(if(input.positions[position].isEmpty())"未选" else "${input.positions[position].size}选")
                                    }
                                    DigitGrid(input.positions[position],"pos$position",Modifier.weight(1f)){digit->model.updateInput{current->current.copy(positions=current.positions.mapIndexed{i,set->if(i==position)set.toggle(digit) else set},excluded=emptySet())}}
                                }
                            }
                        }
                        PickMethod.FILTER,PickMethod.DAN_TUO -> {
                            Row(Modifier.fillMaxWidth().heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically){
                                Column(Modifier.weight(1f)){Text(methodLabel(input.method),style=MaterialTheme.typography.titleMedium);SmallLabel("检查结果后加入本期")}
                                TextButton(onClick=onTools){Text("调整条件")}
                            }
                        }
                    }
                    if(input.method!=PickMethod.SINGLE&&result.numbers.isNotEmpty()) {
                        HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                        SmallLabel("组合预览",Modifier.padding(top=6.dp,bottom=2.dp))
                        result.numbers.take(if(input.method==PickMethod.POSITION)4 else 12).chunked(4).forEach { row->
                            Row(Modifier.fillMaxWidth().padding(vertical=3.dp)){
                                row.forEach{Text(it,Modifier.weight(1f),fontFamily=NumberFont,fontSize=16.sp,color=MaterialTheme.colorScheme.primary)}
                                repeat(4-row.size){Spacer(Modifier.weight(1f))}
                            }
                        }
                    }
                    if(result.problem!=null&&result.problem!=PickProblem.INCOMPLETE) Text(when(result.problem){PickProblem.NO_RESULTS->"没有符合条件的号码，请调整条件";PickProblem.INVALID_SHAPE->"号码不符合当前组选类型";else->"请检查输入与筛选条件"},color=MaterialTheme.colorScheme.error,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
                }
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(start=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text("${result.numbers.size} 个号码 · ${result.numbers.size*draft.multiplier} 注",Modifier.weight(1f).testTag("pick_count"),style=MaterialTheme.typography.bodySmall)
                    TextButton(onClick=onDetails,enabled=result.numbers.isNotEmpty()||input.excluded.isNotEmpty(),modifier=Modifier.semantics{contentDescription="查看 ${result.numbers.size} 个号码"},contentPadding=PaddingValues(horizontal=8.dp)){Text("明细");Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}
                    IconButton(onClick={help=true}){Icon(Icons.Outlined.HelpOutline,"玩法说明",Modifier.size(20.dp))}
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(color=MaterialTheme.colorScheme.surface) {
            Column {
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        SmallLabel("合计金额")
                        Text("¥ ${result.numbers.size*draft.multiplier*2}",fontFamily=NumberFont,fontWeight=FontWeight.SemiBold,fontSize=if(result.numbers.size*draft.multiplier>=5000)17.sp else 21.sp,maxLines=1,modifier=Modifier.testTag("pick_amount"))
                    }
                    Surface(shape=RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.surfaceContainerLow,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={model.updateDraft{it.copy(multiplier=(it.multiplier-1).coerceAtLeast(1))}},enabled=draft.multiplier>1){Icon(Icons.Outlined.Remove,"减少倍数",Modifier.size(18.dp))}
                            Text("${draft.multiplier}倍",Modifier.width(32.dp),fontSize=14.sp,fontWeight=FontWeight.Medium)
                            IconButton(onClick={model.updateDraft{it.copy(multiplier=(it.multiplier+1).coerceAtMost(99))}},enabled=draft.multiplier<99){Icon(Icons.Outlined.Add,"增加倍数",Modifier.size(18.dp))}
                        }
                    }
                    Button(onClick=model::save,modifier=Modifier.width(98.dp).height(48.dp).testTag("save_plan"),shape=RoundedCornerShape(10.dp),contentPadding=PaddingValues(horizontal=8.dp),enabled=state.ready&&!state.saving&&result.problem==null&&draft.issue.length==7){
                        if(state.saving) CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp,color=MaterialTheme.colorScheme.onPrimary)
                        else Text(if(draft.editingId==null)"加入本期" else "更新记录")
                    }
                }
            }
        }
    }
    if(issueEditor) {
        var value by remember { mutableStateOf(draft.issue) }
        WorkspaceSheet("选择目标期号",{issueEditor=false}) {
            OutlinedTextField(value,{if(it.length<=7&&it.all{c->c in '0'..'9'})value=it},label={Text("目标期号")},placeholder={Text("七位期号，例如 2026300")},singleLine=true,
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth().testTag("issue_input"))
            Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically){SmallLabel("已开奖期不能新增或改写方案",Modifier.weight(1f));Button(onClick={model.updateDraft{it.copy(issue=value)};issueEditor=false},enabled=value.length==7,modifier=Modifier.testTag("apply_issue")){Text("应用期号")}}
        }
    }
    if(batch) {
        var text by remember { mutableStateOf("") }
        var invalid by remember { mutableStateOf(false) }
        WorkspaceSheet("批量录入${if(input.method==PickMethod.GROUP)playLabel(input.play) else "单选"}",{batch=false}) {
            OutlinedTextField(text,{text=it;invalid=false},label={Text("三位号码")},placeholder={Text("007 012 268")},supportingText={Text(if(invalid)"请检查三位号码和组选类型" else "用空格、逗号或换行分隔，每个号码保留三位")},minLines=3,isError=invalid,modifier=Modifier.fillMaxWidth().testTag("batch_input"))
            Row(Modifier.fillMaxWidth().padding(top=8.dp),horizontalArrangement=Arrangement.End){TextButton(onClick={batch=false}){Text("取消")};Button(onClick={
                val numbers=text.trim().split(Regex("[\\s,，;；]+"))
                val updated=input.copy(manual=(input.manual+numbers).distinct(),byNumber=input.method==PickMethod.GROUP,excluded=emptySet())
                if(PickEngine.generate(updated).problem==null){model.updateInput{updated};batch=false}else invalid=true
            }){Text("加入号码")}}
        }
    }
    if(help) WorkspaceSheet("四种选号方式",{help=false}) {
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            listOf("单选" to "三位数字与顺序完全一致，输满三位自动收录。","组选" to "组三两同一异；组六三位各异，均不分顺序。","单选单" to "从数字池中取三个不同数字，展开所有顺序，按单选核对。","位选" to "分别选百、十、个位，生成全部组合，按单选核对。").forEach{(title,body)->Column{Text(title,fontWeight=FontWeight.SemiBold);SmallLabel(body)}}
            SmallLabel("每注 2 元，金额用于整理个人方案。")
        }
    }
}

fun Set<Int>.toggle(digit:Int)=if(digit in this)this-digit else this+digit
@Composable fun DigitGrid(selected:Set<Int>,tag:String,modifier:Modifier=Modifier,onDigit:(Int)->Unit) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(2.dp)) {
        repeat(2){row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            repeat(5){column->val digit=row*5+column;val active=digit in selected
                Surface(onClick={onDigit(digit)},modifier=Modifier.weight(1f).height(48.dp).testTag("${tag}_$digit").semantics{this.selected=active;contentDescription="$digit"},shape=RoundedCornerShape(8.dp),
                    color=if(active)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor=if(active)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    border=BorderStroke(if(active)1.dp else .5.dp,if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                    Box(contentAlignment=Alignment.Center){Text(digit.toString(),fontFamily=NumberFont,fontWeight=if(active)FontWeight.Bold else FontWeight.Medium,fontSize=20.sp);if(active)Icon(Icons.Outlined.Check,null,Modifier.align(Alignment.TopEnd).padding(3.dp).size(10.dp))}
                }
            }
        }}
    }
}
@Composable fun PoolTools(selected:Set<Int>,onChange:(Set<Int>)->Unit) {
    var more by remember{mutableStateOf(false)}
    Row(Modifier.fillMaxWidth().height(48.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("数字池",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.width(8.dp));SmallLabel("已选 ${selected.size} 个",Modifier.weight(1f))
        TextButton(onClick={onChange((0..9).toSet())},contentPadding=PaddingValues(horizontal=8.dp)){Text("全选")}
        Box { IconButton(onClick={more=true}){Icon(Icons.Outlined.MoreHoriz,"数字池操作")}
            DropdownMenu(more,{more=false}){DropdownMenuItem(text={Text("反选数字")},onClick={onChange((0..9).toSet()-selected);more=false});DropdownMenuItem(text={Text("清空数字")},onClick={onChange(emptySet());more=false})}
        }
    }
}
