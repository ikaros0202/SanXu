package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.pick.*
import com.sanxu.domain.filter.*
import com.sanxu.domain.repository.SavedMethod
import com.sanxu.domain.attributes.GroupShape
import com.sanxu.feature.pick.ConditionEditor
import com.sanxu.ui.*

@Composable fun ToolsPage(model:WorkspaceViewModel,onPick:()->Unit,onBacktest:()->Unit) {
    val state by model.editor.collectAsStateWithLifecycle()
    val methods by model.methods.collectAsStateWithLifecycle()
    val input=state.draft.input
    val conditions=remember(input.conditionsJson){if(input.conditionsJson.isBlank())emptyList() else ConditionCodec.decode(input.conditionsJson)}
    val result=remember(input){PickEngine.generate(input)}
    var section by rememberSaveable{mutableIntStateOf(0)}
    var editorIndex by remember{mutableStateOf<Int?>(null)}
    var naming by remember{mutableStateOf(false)}
    var delete by remember{mutableStateOf<SavedMethod?>(null)}
    var detail by remember{mutableStateOf<SavedMethod?>(null)}
    Column(Modifier.fillMaxSize()) {
        ChoiceRail(listOf("筛选","模板","备注"),section,{section=it},Modifier.padding(horizontal=12.dp),tags=listOf("tool_filters","tool_templates","tool_note"))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            when(section) {
                0->WorkspacePanel {
                    PanelHeading("生成范围"){ContextTag(methodLabel(input.method),Modifier.padding(end=8.dp))}
                    Column(Modifier.padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        ChoiceRail(listOf("全号码筛选","胆拖选号"),when(input.method){PickMethod.FILTER->0;PickMethod.DAN_TUO->1;else->-1},{model.chooseMethod(if(it==0)PickMethod.FILTER else PickMethod.DAN_TUO)})
                        if(input.method==PickMethod.FILTER||input.method==PickMethod.DAN_TUO)ChoiceRail(PlayType.entries.map(::playLabel),PlayType.entries.indexOf(input.play),{index->model.updateInput{it.copy(play=PlayType.entries[index],excluded=emptySet())}})
                        if(input.method==PickMethod.DAN_TUO) {
                            Text("胆码 · 必须包含",fontWeight=FontWeight.Medium,fontSize=13.sp,modifier=Modifier.padding(top=8.dp))
                            DigitGrid(input.dan,"dan"){digit->model.updateInput{it.copy(dan=it.dan.toggle(digit),tuo=it.tuo-digit,excluded=emptySet())}}
                            Text("拖码 · 参与组合",fontWeight=FontWeight.Medium,fontSize=13.sp,modifier=Modifier.padding(top=8.dp))
                            DigitGrid(input.tuo,"tuo"){digit->model.updateInput{it.copy(tuo=it.tuo.toggle(digit),dan=it.dan-digit,excluded=emptySet())}}
                            SmallLabel("胆码与拖码互斥；组三最多1胆，组六最多2胆。")
                        } else SmallLabel("在当前选号范围内继续筛选，返回后保留输入。",Modifier.padding(vertical=8.dp))
                    }
                    HorizontalDivider(Modifier.padding(top=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
                    PanelHeading("筛选条件 · ${conditions.size}"){
                        TextButton(onClick={editorIndex=-1},modifier=Modifier.testTag("add_condition")){Icon(Icons.Outlined.Add,null,Modifier.size(18.dp));Text("添加")}
                    }
                    if(conditions.isEmpty())SmallLabel("尚未添加条件，保留当前全部号码。",Modifier.padding(start=16.dp,end=16.dp,bottom=12.dp))
                    conditions.forEachIndexed{index,condition->
                        Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable{editorIndex=index}.padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f).padding(vertical=8.dp)){Text(condition.title,fontSize=13.sp,fontWeight=FontWeight.Medium);SmallLabel(conditionDescription(condition))}
                            IconButton(onClick={model.updateInput{it.copy(conditionsJson=ConditionCodec.encode(conditions.filterIndexed{i,_->i!=index}),excluded=emptySet())}}){Icon(Icons.Outlined.Close,"移除${condition.title}",Modifier.size(18.dp))}
                        }
                        if(index!=conditions.lastIndex)HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
                    }
                    if(result.problem!=null&&result.problem!=PickProblem.NO_RESULTS&&result.problem!=PickProblem.INCOMPLETE)Text("请检查号码、胆码或拖码条件",Modifier.padding(12.dp),fontSize=12.sp,color=MaterialTheme.colorScheme.error)
                }
                1->WorkspacePanel {
                    PanelHeading("方法模板"){TextButton(onClick={naming=true}){Icon(Icons.Outlined.Add,null,Modifier.size(18.dp));Text("保存当前方法")}}
                    if(methods.isEmpty())SmallLabel("保存当前数字池、位置与条件，下次直接应用。不包含期号和倍数。",Modifier.padding(start=16.dp,end=16.dp,bottom=12.dp))
                    methods.forEach{method->
                        Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable{detail=method}.padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f).padding(vertical=8.dp)){Text(method.title,fontWeight=FontWeight.Medium,fontSize=13.sp,maxLines=2,overflow=TextOverflow.Ellipsis);SmallLabel("${methodLabel(method.input.method)} · ${PickEngine.generate(method.input).numbers.size} 个号码")}
                            TextButton(onClick={model.applyMethod(method);onPick()}){Text("应用")}
                            IconButton(onClick={delete=method}){Icon(Icons.Outlined.DeleteOutline,"删除模板",Modifier.size(20.dp))}
                        }
                        HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                2->WorkspacePanel {
                    PanelHeading("这次选号的备注"){SmallLabel("${state.draft.note.length}/200",Modifier.padding(end=8.dp))}
                    OutlinedTextField(state.draft.note,{note->if(note.length<=200)model.updateDraft{it.copy(note=note)}},placeholder={Text("记下这一期的思路，随选号记录保存。")},minLines=5,maxLines=8,modifier=Modifier.fillMaxWidth().padding(start=12.dp,end=12.dp,bottom=16.dp).testTag("draft_note"),shape=RoundedCornerShape(6.dp))
                }
            }
        }
        Surface(color=MaterialTheme.colorScheme.surface) {Column{
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text("${result.numbers.size} 个号码",style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("tools_count"));SmallLabel(methodLabel(input.method))}
                TextButton(onClick=onBacktest,enabled=result.problem==null){Text("历史回测")}
                Button(onClick=onPick,modifier=Modifier.height(48.dp).testTag("tools_done"),shape=RoundedCornerShape(6.dp)){Text("返回选号")}
            }
        }}
    }
    editorIndex?.let{index->ConditionEditor(conditions.getOrNull(index),onDismiss={editorIndex=null},onConfirm={condition->
        val updated=conditions.toMutableList().apply{if(index<0)add(condition) else set(index,condition)}
        model.updateInput{it.copy(conditionsJson=ConditionCodec.encode(updated),excluded=emptySet())};editorIndex=null
    })}
    if(naming) {
        var title by remember{mutableStateOf("")}
        WorkspaceSheet("保存方法模板",{naming=false}){
            OutlinedTextField(title,{if(it.length<=40)title=it},label={Text("模板名称")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically){SmallLabel("${methodLabel(input.method)} · ${result.numbers.size} 个号码",Modifier.weight(1f));Button(onClick={model.saveMethod(title,30);naming=false},enabled=title.isNotBlank()){Text("保存")}}
        }
    }
    delete?.let{method->WorkspaceSheet("删除模板？",{delete=null}){Text(method.title,Modifier.padding(vertical=12.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={delete=null}){Text("取消")};Button(onClick={model.deleteMethod(method.id);delete=null}){Text("删除")}}}}
    detail?.let{method->WorkspaceSheet("方法详情",{detail=null}){Text(method.title,Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.titleMedium);Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){SmallLabel("${methodLabel(method.input.method)} · ${PickEngine.generate(method.input).numbers.size} 个号码",Modifier.weight(1f));Button(onClick={model.applyMethod(method);detail=null;onPick()}){Text("应用方法")}}}}
}

fun conditionDescription(c:FilterCondition):String {
    fun pos(p:Position)=listOf("百位","十位","个位")[p.ordinal]
    fun nums(v:Set<Int>)=v.sorted().joinToString("、")
    return when(c){
        is GlobalRequiredDigits->"包含 ${nums(c.digits)}";is GlobalExcludedDigits->"排除 ${nums(c.digits)}";is PositionAllowed->nums(c.allowedDigits)
        is SumRange->"${c.minimum} 至 ${c.maximum}";is SpanRange->"${c.minimum} 至 ${c.maximum}";is SumTailAllowed->nums(c.values)
        is OddCountAllowed->"${nums(c.counts)} 个";is BigCountAllowed->"${nums(c.counts)} 个";is PrimeLikeCountAllowed->"${nums(c.counts)} 个（1、2、3、5、7）"
        is RouteAllowed->"${pos(c.position)}：${nums(c.routes)} 路"
        is GroupShapeCondition->c.values.joinToString("、"){when(it){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"}}
        is ConsecutiveCondition->if(c.requirePair)"包含相邻数字" else "不包含相邻数字"
        is PairRelationRange->"${listOf("百十位","十个位","百个位")[c.position.ordinal]}${if(c.metric==PairMetric.SUM)"之和" else "差值"}：${c.minimum} 至 ${c.maximum}"
        is DanTuoCondition->"胆码 ${nums(c.danDigits)}；拖码 ${nums(c.tuoDigits)}"
    }
}
