package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.sanxu.domain.omission.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.livecontent.LiveContentRefreshState
import com.sanxu.domain.livecontent.TrialVisibility
import com.sanxu.domain.livecontent.LiveRefreshTrigger
import com.sanxu.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while(isActive) {
                value=ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                model.live.refreshTrial(LiveRefreshTrigger.HOME_VISIBLE)
                delay(30_000)
            }
        }
    }
    val trial=TrialVisibility.visible(candidateTrial,now)
    val trialState by model.trialState.collectAsStateWithLifecycle()
    val announcement by model.announcement.collectAsStateWithLifecycle()
    val metadata by model.metadata.collectAsStateWithLifecycle()
    val refreshing by model.refreshing.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val latest=draws.lastOrNull()
    val checked=entries.map { it.issue }.distinct().count { issue->draws.any { it.issue==issue } }
    val pending=entries.filter { entry->draws.none { it.issue==entry.issue } }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Row(Modifier.fillMaxWidth().height(66.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Grain,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(26.dp))
                    Text("叁序",fontSize=27.sp,fontWeight=FontWeight.Bold,letterSpacing=3.sp)
                }
                SmallLabel("三个数字，各有次序")
            }
            IconButton(onClick=onSettings){Icon(Icons.Outlined.Tune,"偏好设置")}
        } }
        item {
            if(latest==null) EmptyPanel("正在读取开奖","首次打开正在准备历史数据。")
            else Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable{onHistory(latest.issue)}) {
                            Text("第 ${latest.issue} 期",fontWeight=FontWeight.SemiBold)
                            SmallLabel("${latest.drawDate} · 最近开奖  ›")
                        }
                        if(refreshing) CircularProgressIndicator(Modifier.padding(12.dp).size(22.dp),strokeWidth=2.dp)
                        else IconButton(onClick={model.refresh()}){Icon(Icons.Outlined.Refresh,"更新开奖")}
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        latest.number.digits.forEachIndexed { index,digit ->
                            Surface(Modifier.weight(1f),shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.primaryContainer) {
                                Column(Modifier.padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text(digit.toString(),fontSize=48.sp,fontWeight=FontWeight.Medium,fontFamily=NumberFont,color=MaterialTheme.colorScheme.primary)
                                    SmallLabel(listOf("百位","十位","个位")[index])
                                }
                            }
                        }
                    }
                    val attributes=remember(latest){DrawAttributes.calculate(latest.number)}
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        Text("和值 ${attributes.sum}",fontSize=13.sp)
                        Text("跨度 ${attributes.span}",fontSize=13.sp)
                        Text(attributes.parityPattern,fontSize=13.sp)
                        Text(when(attributes.groupShape){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"},fontSize=13.sp)
                    }
                    if(metadata?.lastFailureType!=null) {
                        val lastSuccess=metadata?.lastSuccessEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) }
                        Text("更新未成功 · ${lastSuccess?.let { "上次成功 $it" } ?: "保留 ${latest.drawDate} 开奖"}",Modifier.padding(top=8.dp),fontSize=12.sp,color=MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if(latest!=null) item {
            val before=remember(draws){draws.dropLast(1).map { it.number }}
            Row(Modifier.fillMaxWidth().padding(top=2.dp,bottom=2.dp),verticalAlignment=Alignment.CenterVertically) {
                Text("号码回顾",fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));SmallLabel("开出前 · 近30期")
            }
            Row(Modifier.fillMaxWidth().padding(top=6.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                latest.number.digits.forEachIndexed { index,digit ->
                    val values=remember(before,index){before.map { it.digits[index] }}
                    val missed=remember(values,digit){OmissionCalculator.calculate(values,digit).currentOmission}
                    val count=values.takeLast(30).count { it==digit }
                    Surface(Modifier.weight(1f),shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${listOf("百","十","个")[index]}位  $digit",fontFamily=NumberFont,fontWeight=FontWeight.Bold)
                            SmallLabel("此前遗漏 $missed 期")
                            val heat=when { count<2.4->"冷";count<3.6->"温";else->"热" }
                            SmallLabel("30期 $count 次 ·$heat")
                        }
                    }
                }
            }
        }
        item { Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.fillMaxWidth().padding(start=14.dp,end=4.dp,top=6.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("试机号",fontWeight=FontWeight.SemiBold); SmallLabel(trial?.let { "第 ${it.issue} 期" } ?: "暂无本地记录"); if(trialState is LiveContentRefreshState.Failed) SmallLabel("更新未成功，可重试") }
                Text(trial?.number ?: "— — —",fontFamily=NumberFont,fontSize=25.sp,fontWeight=FontWeight.Bold)
                if(trialState==LiveContentRefreshState.Refreshing) CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp)
                else IconButton(onClick=model::refreshTrial){Icon(Icons.Outlined.Refresh,if(trialState is LiveContentRefreshState.Failed) "重试试机号" else "刷新试机号")}
            }
        } }
        item { Surface(onClick=if(pending.isEmpty()) onPick else onPlan,shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.FolderOpen,null,tint=MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(horizontal=12.dp)) { Text(if(pending.isEmpty()) "开始整理一期方案" else "${pending.map { it.issue }.distinct().size} 期方案待开奖",fontWeight=FontWeight.SemiBold);SmallLabel(if(settings.showReplayNotice&&checked>0) "$checked 期已开奖，可核对号码" else "四种选号方式，统一整理") }
                Icon(Icons.Outlined.ChevronRight,null)
            }
        } }
        item { Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable { onHistory(latest?.issue.orEmpty()) },verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("云南开奖公告",style=MaterialTheme.typography.bodyMedium)
                val verified=announcement?.takeIf { it.issue==latest?.issue&&it.number==latest.number }
                SmallLabel(verified?.let { "销售 ${formatMoney(it.salesAmountYuan)} 元 · 查看详情" } ?: "进入档案查看各期公告")
            };Icon(Icons.Outlined.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
        } }
    }
}

fun formatMoney(value:Long)=NumberFormat.getIntegerInstance().format(value)

@Composable fun HistoryPage(model:WorkspaceViewModel,initialIssue:String) {
    val draws by model.draws.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var chosen by rememberSaveable { mutableStateOf(initialIssue) }
    val filtered=remember(draws,query){draws.asReversed().filter { query.isBlank()||it.issue.contains(query)||it.drawDate.contains(query) }}
    var detail by remember { mutableStateOf<DrawRecord?>(null) }
    LaunchedEffect(initialIssue,draws.isEmpty()) { detail=draws.firstOrNull { it.issue==chosen } }
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("期号、年份或日期")},placeholder={Text("例如 2026001 / 2026-07")},leadingIcon={Icon(Icons.Outlined.Search,null)})
        SmallLabel("共 ${filtered.size} 期",Modifier.padding(vertical=10.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(filtered,key={it.issue}) { draw ->
                Row(Modifier.fillMaxWidth().heightIn(min=64.dp).clickable{chosen=draw.issue;detail=draw},verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)){Text("第 ${draw.issue} 期");SmallLabel(draw.drawDate)}
                    Text(draw.number.value,fontSize=25.sp,fontFamily=NumberFont,color=MaterialTheme.colorScheme.primary,letterSpacing=5.sp)
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.padding(start=12.dp))
                };HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
    detail?.let { draw ->
        var announcement by remember(draw.issue){mutableStateOf<YunnanAnnouncement?>(null)}
        var loading by remember(draw.issue){mutableStateOf(true)}
        var retry by remember { mutableIntStateOf(0) }
        LaunchedEffect(draw.issue,retry){loading=true;announcement=model.announcement(draw.issue);loading=false}
        val a=remember(draw){DrawAttributes.calculate(draw.number)}
        AlertDialog(onDismissRequest={detail=null},title={Text("第 ${draw.issue} 期")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(draw.number.value,fontFamily=NumberFont,fontSize=38.sp,letterSpacing=8.sp,color=MaterialTheme.colorScheme.primary)
            SmallLabel(draw.drawDate)
            Text("和值 ${a.sum} · 和尾 ${a.sumTail} · 跨度 ${a.span}")
            Text("${a.parityPattern} · ${a.sizePattern} · ${a.qualityPattern}")
            Text("012路位置 ${a.routePositionPattern} · 个数比 ${a.routeCountPattern}")
            HorizontalDivider()
            Text("云南公告",fontWeight=FontWeight.SemiBold)
            if(loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            else if(announcement==null) { SmallLabel("暂未取得这一期公告");TextButton(onClick={retry++}){Text("重试公告")}}
            announcement?.takeIf { it.number==draw.number }?.let { report->
                Text("销售额 ${formatMoney(report.salesAmountYuan)} 元")
                SmallLabel("中奖总额 ${formatMoney(report.winningTotalYuan)} 元")
                report.plays.forEach { play->SmallLabel("${play.playType.displayName}   ${formatMoney(play.winningCount)} 注 × ${formatMoney(play.prizePerBetYuan)} 元") }
                report.payoutSummary?.let { SmallLabel("派奖：$it") }
                report.prizePoolBalanceFen?.let { SmallLabel("奖池 ${"%.2f".format(it/100.0)} 元") }
                report.redemptionDeadline?.let { SmallLabel("兑奖截止 $it") }
            }
        }},confirmButton={TextButton(onClick={detail=null}){Text("关闭")}})
    }
}
