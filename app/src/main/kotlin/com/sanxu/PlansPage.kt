package com.sanxu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.sanxu.domain.pick.*
import com.sanxu.ui.*

@Composable fun PlansPage(model:WorkspaceViewModel,onPick:()->Unit) {
    val entries by model.entries.collectAsStateWithLifecycle()
    val draws by model.draws.collectAsStateWithLifecycle()
    var past by rememberSaveable { mutableStateOf(false) }
    val allIssues=remember(entries){entries.map { it.issue }.distinct().sortedDescending()}
    val pastIssues=allIssues.filter { issue->draws.lastOrNull()?.issue?.let { issue<=it }==true }
    val issues=if(past)pastIssues else allIssues-pastIssues.toSet()
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val issue=chosen?.takeIf { it in issues } ?: issues.firstOrNull()
    val sources=remember(entries,issue){entries.filter { it.issue==issue }}
    val summary=remember(sources){PickEngine.merge(sources)}
    val draw=draws.firstOrNull { it.issue==issue }
    val locked=issue!=null&&draws.lastOrNull()?.issue?.let { issue<=it }==true
    var menu by remember { mutableStateOf(false) }
    var allNumbers by remember { mutableStateOf(false) }
    var remove by remember { mutableStateOf<PlanEntry?>(null) }
    if(entries.isEmpty()) { EmptyPanel("第一期方案，从这里开始","在选号页选择号码，加入后会按期号自动整理。","去选号",onPick);return }
    Column(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        FilterChip(!past,{past=false},label={Text("未开奖 ${allIssues.size-pastIssues.size}")})
        FilterChip(past,{past=true},label={Text("已开奖 ${pastIssues.size}")})
    }
    if(issues.isEmpty()) EmptyPanel("这里还没有方案",if(past) "已有方案开奖后，会在这里显示核对结果。" else "可以从选号页创建新一期方案。")
    else LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Box { TextButton(onClick={menu=true}) { Text("第 $issue 期",fontSize=18.sp,fontWeight=FontWeight.Bold);Icon(Icons.Outlined.KeyboardArrowDown,null) }
                DropdownMenu(menu,{menu=false}){issues.forEach { value->DropdownMenuItem(text={Text("第 $value 期")},onClick={chosen=value;menu=false}) }}
            }
            Spacer(Modifier.weight(1f));SmallLabel(if(draw!=null) "已开奖" else if(locked) "等待补齐开奖" else "待开奖")
        } }
        item { Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.onBackground,contentColor=MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Column { Text("合计金额",fontSize=12.sp);Text("¥ ${summary.amountYuan}",fontSize=32.sp,fontFamily=NumberFont,fontWeight=FontWeight.SemiBold,modifier=Modifier.testTag("plan_amount")) }
                    Column(horizontalAlignment=Alignment.End) {Text("${summary.numberCount} 个号码",fontSize=18.sp,modifier=Modifier.testTag("plan_count"));Text("${summary.unitCount} 注 · ${sources.size} 条记录",fontSize=13.sp,modifier=Modifier.testTag("plan_units"))}
                }
                if(draw!=null) {
                    HorizontalDivider(Modifier.padding(vertical=12.dp),color=MaterialTheme.colorScheme.background.copy(alpha=.2f))
                    val matches=summary.bets.filter { PickEngine.matches(it,draw.number.value) }
                    Text("开奖 ${draw.number.value}   ·   ${if(matches.isEmpty()) "本期未命中" else "命中 ${matches.sumOf { it.multiplier }} 注"}",modifier=Modifier.testTag("replay_result"))
                }
            }
        } }
        item { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("合并号码",fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
            TextButton(onClick={allNumbers=true}){Text("全部 ${summary.numberCount} 个");Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp))}
        }
            summary.bets.take(8).chunked(2).forEach { row-> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach { bet-> Surface(Modifier.weight(1f).padding(bottom=8.dp),shape=RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically) { Text(bet.number,fontFamily=NumberFont,fontWeight=FontWeight.Bold,fontSize=18.sp);Spacer(Modifier.weight(1f));SmallLabel("${playLabel(bet.play)} ×${bet.multiplier}") }
                } }
                if(row.size==1) Spacer(Modifier.weight(1f))
            } }
            SmallLabel("同玩法、同号码已合并，倍数相加")
        }
        item { Text("选号记录",fontWeight=FontWeight.SemiBold) }
        items(sources,key={it.id}) { entry->
            Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Text(methodLabel(entry.input.method),fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));SmallLabel("${entry.numbers.size} 个 × ${entry.multiplier} 倍")
                    }
                    Text(entry.numbers.take(8).joinToString("  ")+if(entry.numbers.size>8) " …" else "",fontFamily=NumberFont,modifier=Modifier.padding(top=8.dp))
                    if(entry.note.isNotBlank()) Text(entry.note,style=MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                        if(!locked) {
                            TextButton(onClick={remove=entry}){Text("删除")}
                            TextButton(onClick={model.edit(entry);onPick()}){Text("编辑这条")}
                        }
                        TextButton(onClick={model.edit(entry,copy=true);onPick()}){Text("复制选号")}
                    }
                }
            }
        }
        item { OutlinedButton(onClick=onPick,modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.Add,null);Text("继续选号")} }
    }
    }
    if(allNumbers) AlertDialog(onDismissRequest={allNumbers=false},title={Text("第 $issue 期 · 合并明细")},text={
        LazyColumn(Modifier.heightIn(max=430.dp)) { items(summary.bets,key={"${it.play}_${it.number}"}) { bet->
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(bet.number,fontFamily=NumberFont,fontSize=22.sp,modifier=Modifier.weight(1f))
                Text("${playLabel(bet.play)} ×${bet.multiplier}")
                if(draw!=null&&PickEngine.matches(bet,draw.number.value)) Icon(Icons.Outlined.CheckCircle,"命中",Modifier.padding(start=6.dp),tint=MaterialTheme.colorScheme.primary)
            };HorizontalDivider()
        } }
    },confirmButton={TextButton(onClick={allNumbers=false}){Text("关闭")}})
    remove?.let { entry->AlertDialog(onDismissRequest={remove=null},title={Text("删除这条选号记录？")},text={Text("将扣除这条记录贡献的 ${entry.numbers.size} 个号码 × ${entry.multiplier} 倍。其他记录仍然保留。")},confirmButton={TextButton(onClick={model.remove(entry);remove=null}){Text("删除记录")}},dismissButton={TextButton(onClick={remove=null}){Text("取消")}}) }
}
