package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.testTag
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
    val metadata by model.metadata.collectAsStateWithLifecycle()
    val refreshing by model.refreshing.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val latest=draws.lastOrNull()
    val checked=entries.map{it.issue}.distinct().count{issue->draws.any{it.issue==issue}}
    val pending=entries.filter{entry->draws.none{it.issue==entry.issue}}.map{it.issue}.distinct().size
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=12.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item {
            Surface(onClick={latest?.let{onHistory(it.issue)}},shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.secondaryContainer,contentColor=MaterialTheme.colorScheme.onSecondaryContainer) {
                if(latest==null) EmptyPanel("正在读取开奖","正在准备历史记录。")
                else Column {
                    Row(Modifier.fillMaxWidth().height(48.dp).padding(start=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                        Row(Modifier.weight(1f).fillMaxHeight().clickable{onHistory(latest.issue)},verticalAlignment=Alignment.CenterVertically) {
                            Text("第 ${latest.issue} 期",style=MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.weight(1f))
                            Text(latest.drawDate.takeLast(5),fontFamily=NumberFont,fontSize=12.sp)
                        }
                        if(refreshing)CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp,color=LocalContentColor.current)
                        else IconButton(onClick=model::refresh){Icon(Icons.Outlined.Refresh,"更新开奖",Modifier.size(20.dp))}
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal=28.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                        latest.number.digits.forEachIndexed{index,digit->Column(horizontalAlignment=Alignment.CenterHorizontally) {
                            Surface(shape=CircleShape,color=MaterialTheme.colorScheme.onSecondaryContainer,contentColor=MaterialTheme.colorScheme.secondaryContainer) {
                                Box(Modifier.size(48.dp),contentAlignment=Alignment.Center){Text(digit.toString(),fontFamily=NumberFont,fontSize=30.sp,fontWeight=FontWeight.Bold)}
                            }
                            Text(listOf("百位","十位","个位")[index],fontSize=11.sp,lineHeight=14.sp,modifier=Modifier.padding(top=2.dp))
                        }}
                    }
                    if(metadata?.lastFailureType!=null) {
                        val last=metadata?.lastSuccessEpochMillis?.let{Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))}
                        Text("更新未成功 · ${last?.let{"上次成功 $it"} ?: "保留 ${latest.drawDate} 开奖"}",Modifier.padding(horizontal=12.dp).heightIn(min=20.dp),fontSize=11.sp,color=LocalContentColor.current)
                    }
                    val a=remember(latest){DrawAttributes.calculate(latest.number)}
                    Row(Modifier.fillMaxWidth().height(32.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("和值 ${a.sum}  ·  跨度 ${a.span}  ·  ${shapeLabel(a.groupShape)}",Modifier.weight(1f),fontSize=12.sp)
                        Text("详情",fontSize=12.sp);Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp))
                    }
                }
            }
        }
        item { WorkspacePanel {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(start=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                Text("试机号",style=MaterialTheme.typography.titleMedium)
                Text(trial?.number?.toList()?.joinToString(" ") ?: "— — —",Modifier.weight(1f).padding(horizontal=12.dp),fontFamily=NumberFont,fontSize=23.sp,fontWeight=FontWeight.SemiBold)
                Column(horizontalAlignment=Alignment.End){
                    SmallLabel(trial?.let{"${it.issue}期"} ?: "等待本期数据")
                    if(trialState is LiveContentRefreshState.Failed)SmallLabel("待更新")
                }
                if(trialState==LiveContentRefreshState.Refreshing)CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp)
                else IconButton(onClick=model::refreshTrial){Icon(Icons.Outlined.Refresh,if(trialState is LiveContentRefreshState.Failed)"重试试机号" else "刷新试机号",Modifier.size(20.dp))}
            }
        } }
        item { WorkspacePanel {
            Surface(color=MaterialTheme.colorScheme.surfaceContainerLow){
                Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Row(Modifier.weight(1.25f).fillMaxHeight().clickable{onHistory("")},verticalAlignment=Alignment.CenterVertically) {
                        Text("最近开奖",fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                        Icon(Icons.Outlined.ChevronRight,"全部开奖",Modifier.size(16.dp))
                    }
                    SmallLabel("开奖号",Modifier.weight(1f));SmallLabel("和值",Modifier.width(38.dp));SmallLabel("跨度",Modifier.width(32.dp))
                }
            }
            draws.asReversed().drop(1).take(5).forEach{draw->
                val a=remember(draw){DrawAttributes.calculate(draw.number)}
                Row(Modifier.fillMaxWidth().height(48.dp).testTag("recent_${draw.issue}").clickable{onHistory(draw.issue)}.padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(draw.issue,Modifier.weight(1.25f),fontFamily=NumberFont,fontSize=13.sp)
                    Text(draw.number.digits.joinToString(" "),Modifier.weight(1f),fontFamily=NumberFont,fontSize=18.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                    Text(a.sum.toString(),Modifier.width(38.dp),fontFamily=NumberFont,fontSize=13.sp)
                    Text(a.span.toString(),Modifier.width(32.dp),fontFamily=NumberFont,fontSize=13.sp)
                }
                HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.6f))
            }
        } }
        item { WorkspacePanel {
            Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f).heightIn(min=48.dp).clickable(onClick=onPlan).padding(horizontal=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FolderOpen,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(Modifier.padding(start=8.dp)) {
                        Text("我的方案",fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                        SmallLabel(if(settings.showReplayNotice&&checked>0)"$checked 期可核对" else if(pending>0)"$pending 期待开奖" else "还没有待开奖方案")
                    }
                }
                Button(onClick=onPick,modifier=Modifier.height(48.dp).testTag("home_start_pick"),shape=RoundedCornerShape(8.dp),contentPadding=PaddingValues(horizontal=12.dp)){Text("开始选号");Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}
            }
        } }
    }
}

private fun shapeLabel(shape:GroupShape)=when(shape){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"}

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
                    Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable{chosen=draw.issue;detail=draw}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
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
                Text("开奖前回顾 · 近30期",style=MaterialTheme.typography.titleMedium)
                val before=remember(draws,draw.issue){draws.takeWhile{it.issue<draw.issue}.map{it.number}}
                Row(Modifier.fillMaxWidth()) {
                    draw.number.digits.forEachIndexed{index,digit->
                        val values=remember(before,index){before.map{it.digits[index]}}
                        val missed=remember(values,digit){OmissionCalculator.calculate(values,digit).currentOmission}
                        val count=values.takeLast(30).count{it==digit}
                        Column(Modifier.weight(1f)) {
                            SmallLabel("${listOf("百位","十位","个位")[index]} $digit")
                            Text("遗漏 ${missed}期",fontSize=14.sp,fontWeight=FontWeight.Medium)
                            SmallLabel("30期 $count 次 · ${when{count<2.4->"冷";count<3.6->"温";else->"热"}}")
                        }
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
