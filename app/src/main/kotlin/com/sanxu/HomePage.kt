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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sanxu.core.model.*
import com.sanxu.domain.attributes.*
import com.sanxu.domain.omission.OmissionCalculator
import com.sanxu.domain.livecontent.LiveContentRefreshState
import com.sanxu.domain.livecontent.TrialVisibility
import com.sanxu.domain.livecontent.LiveRefreshTrigger
import com.sanxu.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZonedDateTime
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun HomePage(model:WorkspaceViewModel,onSettings:()->Unit,onHistory:(String)->Unit,onPick:()->Unit,onPlan:()->Unit) {
    val draws by model.draws.collectAsStateWithLifecycle()
    val entries by model.entries.collectAsStateWithLifecycle()
    val candidateTrial by model.trial.collectAsStateWithLifecycle()
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val now by produceState(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")),lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){while(isActive){value=ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));model.live.refreshTrial(LiveRefreshTrigger.HOME_VISIBLE);delay(30_000)}}
    }
    val trial=TrialVisibility.visible(candidateTrial,now)
    val trialState by model.trialState.collectAsStateWithLifecycle()
    val announcement by model.announcement.collectAsStateWithLifecycle()
    val metadata by model.metadata.collectAsStateWithLifecycle()
    val refreshing by model.refreshing.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val latest=draws.lastOrNull()
    val checked=entries.map{it.issue}.distinct().count{issue->draws.any{it.issue==issue}}
    val pending=entries.filter{entry->draws.none{it.issue==entry.issue}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(start=16.dp,end=16.dp,top=4.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            WorkspacePanel {
                if(latest==null) EmptyPanel("正在读取开奖","正在准备历史记录。")
                else {
                    Row(Modifier.fillMaxWidth().padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).heightIn(min=56.dp).clickable{onHistory(latest.issue)},verticalArrangement=Arrangement.Center){Text("第 ${latest.issue} 期",style=MaterialTheme.typography.titleMedium);SmallLabel("${latest.drawDate} · 开奖详情 ›")}
                        if(refreshing) CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp)
                        else IconButton(onClick=model::refresh){Icon(Icons.Outlined.Refresh,"更新开奖",Modifier.size(21.dp))}
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
                        latest.number.digits.forEachIndexed{index,digit->
                            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){SmallLabel(listOf("百位","十位","个位")[index]);Text(digit.toString(),fontFamily=NumberFont,fontSize=44.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.primary)}
                            if(index<2) VerticalDivider(Modifier.height(32.dp),color=MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    val attributes=remember(latest){DrawAttributes.calculate(latest.number)}
                    Surface(color=MaterialTheme.colorScheme.surfaceContainerLow) {
                        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp),horizontalArrangement=Arrangement.SpaceBetween){
                            Text("和值 ${attributes.sum}",fontSize=12.sp);Text("跨度 ${attributes.span}",fontSize=12.sp);Text(attributes.parityPattern,fontSize=12.sp)
                            Text(when(attributes.groupShape){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"},fontSize=12.sp)
                        }
                    }
                    if(metadata?.lastFailureType!=null) {
                        val last=metadata?.lastSuccessEpochMillis?.let{Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))}
                        Text("更新未成功 · ${last?.let{"上次成功 $it"} ?: "保留 ${latest.drawDate} 开奖"}",Modifier.padding(horizontal=16.dp,vertical=8.dp),fontSize=12.sp,color=MaterialTheme.colorScheme.error)
                    }
                    val verified=announcement?.takeIf{it.issue==latest.issue&&it.number==latest.number}
                    Row(Modifier.fillMaxWidth().heightIn(min=52.dp).clickable{onHistory(latest.issue)}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                        Icon(Icons.Outlined.ReceiptLong,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(start=10.dp,top=6.dp,bottom=6.dp)){Text("云南公告",fontSize=13.sp,fontWeight=FontWeight.Medium);SmallLabel(verified?.let{"销售 ${formatMoney(it.salesAmountYuan)} 元"} ?: "查看这一期的公告与明细")}
                        Icon(Icons.Outlined.ChevronRight,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { WorkspacePanel {
            Row(Modifier.fillMaxWidth().heightIn(min=68.dp).padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text("试机号",style=MaterialTheme.typography.titleMedium);SmallLabel(trial?.let{"${it.issue} 期"} ?: "等待本期数据")}
                if(trialState is LiveContentRefreshState.Failed) SmallLabel("待更新",Modifier.padding(end=8.dp))
                Text(trial?.number ?: "— — —",fontFamily=NumberFont,fontSize=26.sp,fontWeight=FontWeight.Medium)
                if(trialState==LiveContentRefreshState.Refreshing)CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp)
                else IconButton(onClick=model::refreshTrial){Icon(Icons.Outlined.Refresh,if(trialState is LiveContentRefreshState.Failed)"重试试机号" else "刷新试机号",Modifier.size(20.dp))}
            }
        } }
        if(latest!=null) item { WorkspacePanel {
            PanelHeading("位置回顾"){SmallLabel("开出前 · 近30期",Modifier.padding(end=8.dp))}
            val before=remember(draws){draws.dropLast(1).map{it.number}}
            Row(Modifier.fillMaxWidth().padding(start=16.dp,end=16.dp,bottom=16.dp)) {
                latest.number.digits.forEachIndexed{index,digit->
                    val values=remember(before,index){before.map{it.digits[index]}}
                    val missed=remember(values,digit){OmissionCalculator.calculate(values,digit).currentOmission}
                    val count=values.takeLast(30).count{it==digit}
                    val heat=when{count<2.4->"冷";count<3.6->"温";else->"热"}
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                        SmallLabel("${listOf("百","十","个")[index]}位 $digit")
                        Text("${missed}期",fontFamily=NumberFont,fontSize=21.sp,fontWeight=FontWeight.SemiBold)
                        SmallLabel("此前遗漏")
                        SmallLabel("30期 $count 次 · $heat")
                    }
                }
            }
        } }
        item { Surface(onClick=if(pending.isEmpty())onPick else onPlan,shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.primaryContainer,contentColor=MaterialTheme.colorScheme.onPrimaryContainer) {
            Row(Modifier.fillMaxWidth().heightIn(min=72.dp).padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(Icons.Outlined.FolderOpen,null,Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(start=12.dp)){Text(if(pending.isEmpty())"整理我的本期方案" else "${pending.map{it.issue}.distinct().size} 期方案待开奖",fontWeight=FontWeight.SemiBold,fontSize=15.sp);Text(if(settings.showReplayNotice&&checked>0)"$checked 期已开奖，可核对号码" else if(pending.isEmpty())"单选、组选、单选单、位选" else "查看清单，继续整理号码",fontSize=12.sp)}
                Icon(Icons.Outlined.ArrowForward,null,Modifier.size(20.dp))
            }
        } }
        if(draws.size>1) item { WorkspacePanel {
            PanelHeading("近期档案"){TextButton(onClick={onHistory("")}){Text("全部");Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}}
            Row(Modifier.fillMaxWidth().padding(horizontal=8.dp).padding(bottom=8.dp)){
                draws.dropLast(1).takeLast(3).asReversed().forEach{draw->Column(Modifier.weight(1f).heightIn(min=56.dp).clickable{onHistory(draw.issue)}.padding(horizontal=8.dp),verticalArrangement=Arrangement.Center){SmallLabel(draw.issue);Text(draw.number.value,fontFamily=NumberFont,fontWeight=FontWeight.SemiBold,fontSize=20.sp,letterSpacing=3.sp)}}
            }
        } }
    }
}

fun formatMoney(value:Long)=NumberFormat.getIntegerInstance().format(value)

@Composable fun HistoryPage(model:WorkspaceViewModel,initialIssue:String) {
    val draws by model.draws.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var chosen by rememberSaveable { mutableStateOf(initialIssue) }
    val filtered=remember(draws,query){draws.asReversed().filter{query.isBlank()||it.issue.contains(query)||it.drawDate.contains(query)}}
    var detail by remember { mutableStateOf<DrawRecord?>(null) }
    LaunchedEffect(initialIssue,draws.isEmpty()){detail=draws.firstOrNull{it.issue==chosen}}
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("搜索期号或日期")},leadingIcon={Icon(Icons.Outlined.Search,null)},trailingIcon={SmallLabel("${filtered.size}期",Modifier.padding(end=12.dp))},shape=RoundedCornerShape(12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp)){SmallLabel("期号 / 日期",Modifier.weight(1f));SmallLabel("开奖号码")}
        WorkspacePanel(Modifier.weight(1f)) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered,key={it.issue}){draw->
                    Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable{chosen=draw.issue;detail=draw}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(draw.issue,fontFamily=NumberFont,fontWeight=FontWeight.Medium);SmallLabel(draw.drawDate)}
                        Text(draw.number.value,fontFamily=NumberFont,fontSize=22.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary,letterSpacing=4.sp)
                        Icon(Icons.Outlined.ChevronRight,null,Modifier.padding(start=12.dp).size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(Modifier.padding(horizontal=16.dp),color=MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    detail?.let{draw->
        var announcement by remember(draw.issue){mutableStateOf<YunnanAnnouncement?>(null)}
        var loading by remember(draw.issue){mutableStateOf(true)}
        var retry by remember{mutableIntStateOf(0)}
        LaunchedEffect(draw.issue,retry){loading=true;announcement=model.announcement(draw.issue);loading=false}
        val a=remember(draw){DrawAttributes.calculate(draw.number)}
        WorkspaceSheet("第 ${draw.issue} 期",{detail=null}){
            Column(Modifier.heightIn(max=540.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Text(draw.number.value,Modifier.weight(1f),fontFamily=NumberFont,fontSize=42.sp,letterSpacing=8.sp,color=MaterialTheme.colorScheme.primary);SmallLabel(draw.drawDate)}
                Surface(color=MaterialTheme.colorScheme.surfaceContainerLow,shape=RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(horizontal=12.dp,vertical=8.dp)){
                        listOf("和值" to a.sum.toString(),"跨度" to a.span.toString(),"奇偶" to a.parityPattern,"大小" to a.sizePattern,"质合" to a.qualityPattern,"和尾" to a.sumTail.toString(),"012路" to a.routePositionPattern,"路数比" to a.routeCountPattern).chunked(2).forEach{pairs->Row(Modifier.fillMaxWidth().height(30.dp),verticalAlignment=Alignment.CenterVertically){pairs.forEach{(label,value)->Row(Modifier.weight(1f)){SmallLabel(label,Modifier.width(48.dp));Text(value,fontSize=14.sp,fontWeight=FontWeight.Medium)}}}}
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically){Text("云南公告",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium);if(!loading&&announcement==null)TextButton(onClick={retry++}){Text("重试公告")}}
                if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())
                else if(announcement==null)SmallLabel("暂未取得这一期公告")
                announcement?.takeIf{it.number==draw.number}?.let{report->
                    Row(Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){SmallLabel("销售额（元）");Text(formatMoney(report.salesAmountYuan),fontFamily=NumberFont,fontWeight=FontWeight.SemiBold)};Column(Modifier.weight(1f)){SmallLabel("中奖总额（元）");Text(formatMoney(report.winningTotalYuan),fontFamily=NumberFont,fontWeight=FontWeight.SemiBold)}}
                    HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                    report.plays.forEach{play->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(play.playType.displayName,Modifier.width(64.dp),fontSize=14.sp);SmallLabel("${formatMoney(play.winningCount)} 注",Modifier.weight(1f));Text("${formatMoney(play.prizePerBetYuan)} 元/注",fontSize=14.sp)}}
                    report.payoutSummary?.let{SmallLabel("派奖：$it")}
                    report.prizePoolBalanceFen?.let{SmallLabel("奖池 ${"%.2f".format(it/100.0)} 元")}
                    report.redemptionDeadline?.let{SmallLabel("兑奖截止 $it")}
                }
            }
        }
    }
}
