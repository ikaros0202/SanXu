package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sanxu.ui.*

@Composable fun PlansPage(model:WorkspaceViewModel,onPick:()->Unit) {
    val entries by model.entries.collectAsStateWithLifecycle()
    val draws by model.draws.collectAsStateWithLifecycle()
    var past by rememberSaveable{mutableStateOf(false)}
    val allIssues=remember(entries){entries.map{it.issue}.distinct().sortedDescending()}
    val pastIssues=allIssues.filter{issue->draws.lastOrNull()?.issue?.let{issue<=it}==true}
    val issues=if(past)pastIssues else allIssues-pastIssues.toSet()
    var chosen by rememberSaveable{mutableStateOf<String?>(null)}
    val issue=chosen?.takeIf{it in issues} ?: issues.firstOrNull()
    val sources=remember(entries,issue){entries.filter{it.issue==issue}}
    val summary=remember(sources){PickEngine.merge(sources)}
    val draw=draws.firstOrNull{it.issue==issue}
    val locked=issue!=null&&draws.lastOrNull()?.issue?.let{issue<=it}==true
    var menu by remember{mutableStateOf(false)}
    var section by rememberSaveable{mutableIntStateOf(0)}
    var remove by remember{mutableStateOf<PlanEntry?>(null)}
    var detail by remember{mutableStateOf<PlanEntry?>(null)}
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            ChoiceRail(listOf("未开奖 ${allIssues.size-pastIssues.size}","已开奖 ${pastIssues.size}"),if(past)1 else 0,{past=it==1},Modifier.weight(1f))
            Box {
                TextButton(onClick={menu=true},enabled=issues.isNotEmpty(),contentPadding=PaddingValues(horizontal=4.dp),modifier=Modifier.width(104.dp)){
                    Text(issue ?: "期号",fontFamily=NumberFont,fontSize=13.sp);Icon(Icons.Outlined.ExpandMore,null,Modifier.size(18.dp))
                }
                DropdownMenu(menu,{menu=false}){issues.forEach{value->DropdownMenuItem(text={Text("第 $value 期")},onClick={chosen=value;menu=false})}}
            }
        }
        Spacer(Modifier.height(8.dp))
        if(issues.isEmpty()) {
            WorkspacePanel { EmptyPanel(if(entries.isEmpty())"还没有本期方案" else "这里还没有方案",if(past)"已有方案开奖后，会在这里显示核对结果。" else "选择号码并加入，同一期的记录会自动整理。","去选号",onPick) }
        } else {
            WorkspacePanel(Modifier.weight(1f)) {
                Surface(color=MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){SmallLabel("第 $issue 期 · 合计",Modifier.weight(1f));ContextTag(if(draw!=null)"已核对" else if(locked)"等待补齐开奖" else "待开奖")}
                    Row(Modifier.fillMaxWidth().padding(top=4.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("¥ ${summary.amountYuan}",Modifier.weight(1f).testTag("plan_amount"),fontSize=24.sp,fontFamily=NumberFont,fontWeight=FontWeight.SemiBold)
                        Column(horizontalAlignment=Alignment.End){Text("${summary.numberCount} 个号码",fontSize=16.sp,fontWeight=FontWeight.Medium,modifier=Modifier.testTag("plan_count"));Text("${summary.unitCount} 注 · ${sources.size} 条记录",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.testTag("plan_units"))}
                    }
                    if(draw!=null) {
                        HorizontalDivider(Modifier.padding(vertical=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
                        val matches=summary.bets.filter{PickEngine.matches(it,draw.number.value)}
                        Row(verticalAlignment=Alignment.CenterVertically){Icon(if(matches.isEmpty())Icons.Outlined.RemoveCircleOutline else Icons.Outlined.CheckCircle,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Text("开奖 ${draw.number.value}   ·   ${if(matches.isEmpty())"本期未命中" else "命中 ${matches.sumOf{it.multiplier}} 注"}",fontSize=14.sp,modifier=Modifier.testTag("replay_result"))}
                    }
                }
                }
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                ChoiceRail(listOf("合并号码","选号记录 ${sources.size}"),section,{section=it},Modifier,tags=listOf("plan_numbers","plan_sources"))
                if(section==0) LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=12.dp)) {
                    summary.bets.groupBy{it.play}.forEach{(play,bets)->
                        item(key="heading_$play"){Row(Modifier.fillMaxWidth().height(36.dp),verticalAlignment=Alignment.CenterVertically){Text(playLabel(play),fontWeight=FontWeight.Medium,fontSize=14.sp);Spacer(Modifier.width(8.dp));SmallLabel("${bets.size} 个号码")}}
                        items(bets.chunked(3)){row->
                            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically){
                                row.forEach{bet->Row(Modifier.weight(1f).padding(end=8.dp),verticalAlignment=Alignment.CenterVertically){
                                    Text(bet.number,fontFamily=NumberFont,fontSize=17.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary)
                                    Text(" ×${bet.multiplier}",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                                    if(draw!=null&&PickEngine.matches(bet,draw.number.value))Icon(Icons.Outlined.Check,"命中",Modifier.padding(start=2.dp).size(12.dp))
                                }}
                                repeat(3-row.size){Spacer(Modifier.weight(1f))}
                            }
                            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.6f))
                        }
                    }
                } else LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=8.dp)) {
                    items(sources,key={it.id}){entry->
                        var sourceMenu by remember{mutableStateOf(false)}
                        Row(Modifier.fillMaxWidth().heightIn(min=72.dp).clickable{detail=entry}.padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f).padding(vertical=10.dp)){
                                Text(methodLabel(entry.input.method),fontSize=15.sp,fontWeight=FontWeight.SemiBold)
                                Text(entry.numbers.take(5).joinToString("  ")+if(entry.numbers.size>5)" …" else "",fontFamily=NumberFont,fontSize=13.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                                SmallLabel("${entry.numbers.size} 个 × ${entry.multiplier} 倍${if(entry.note.isNotBlank())" · 有备注" else ""}")
                            }
                            if(!locked)IconButton(onClick={model.edit(entry);onPick()}){Icon(Icons.Outlined.Edit,"编辑记录 ${entry.id}",Modifier.size(20.dp))}
                            Box {
                                IconButton(onClick={sourceMenu=true}){Icon(Icons.Outlined.MoreHoriz,"记录操作 ${entry.id}")}
                                DropdownMenu(sourceMenu,{sourceMenu=false}){
                                    DropdownMenuItem(text={Text("查看详情")},onClick={sourceMenu=false;detail=entry})
                                    DropdownMenuItem(text={Text("复制选号")},onClick={sourceMenu=false;model.edit(entry,copy=true);onPick()})
                                    if(!locked)DropdownMenuItem(text={Text("删除记录")},onClick={sourceMenu=false;remove=entry})
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){SmallLabel(if(locked)"原记录已锁定，可复制到新一期" else if(section==0)"同玩法同号合并，倍数相加" else "每条记录可单独调整",Modifier.weight(1f));Button(onClick=onPick,modifier=Modifier.height(48.dp),shape=RoundedCornerShape(10.dp)){Icon(Icons.Outlined.Add,null,Modifier.size(18.dp));Spacer(Modifier.width(4.dp));Text("继续选号")}}
        }
    }
    detail?.let{entry->WorkspaceSheet("${methodLabel(entry.input.method)} · 记录详情",{detail=null}) {
        Column(Modifier.heightIn(max=460.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("${entry.issue} 期 · ${entry.numbers.size} 个号码 × ${entry.multiplier} 倍",style=MaterialTheme.typography.titleMedium)
            if(entry.note.isNotBlank()){SmallLabel("备注");Text(entry.note,fontSize=14.sp)}
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            Text(entry.numbers.joinToString("   "),fontFamily=NumberFont,fontSize=15.sp,lineHeight=28.sp)
        }
    }}
    remove?.let{entry->WorkspaceSheet("删除这条选号记录？",{remove=null}){
        Text("将扣除这条记录的 ${entry.numbers.size} 个号码 × ${entry.multiplier} 倍。其他来源保留。",Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={remove=null}){Text("取消")};Button(onClick={model.remove(entry);remove=null}){Text("删除记录")}}
    }}
}
