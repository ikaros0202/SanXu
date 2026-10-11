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
import com.sanxu.core.model.*
import com.sanxu.domain.attributes.*
import com.sanxu.domain.omission.OmissionCalculator
import com.sanxu.ui.*
import java.text.NumberFormat

internal fun shapeLabel(shape:GroupShape)=when(shape){GroupShape.LEOPARD->"豹子";GroupShape.GROUP3->"组三";GroupShape.GROUP6->"组六"}

fun formatMoney(value:Long)=NumberFormat.getIntegerInstance().format(value)

@Composable fun HistoryPage(model:WorkspaceViewModel,initialIssue:String) {
    val draws by model.draws.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var chosen by rememberSaveable { mutableStateOf(initialIssue) }
    val filtered=remember(draws,query){draws.asReversed().filter{query.isBlank()||it.issue.contains(query)||it.drawDate.contains(query)}}
    var detail by remember { mutableStateOf<DrawRecord?>(null) }
    LaunchedEffect(initialIssue,draws.isEmpty()){detail=draws.firstOrNull{it.issue==chosen}}
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp)) {
        OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("搜索期号或日期")},leadingIcon={Icon(Icons.Outlined.Search,null)},trailingIcon={SmallLabel("${filtered.size}期",Modifier.padding(end=12.dp))},shape=RoundedCornerShape(6.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=12.dp)){SmallLabel("期号 / 日期",Modifier.weight(1f));SmallLabel("开奖号码")}
        WorkspacePanel(Modifier.weight(1f)) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered,key={it.issue}){draw->
                    Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable{chosen=draw.issue;detail=draw}.padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(draw.issue,fontFamily=NumberFont,fontWeight=FontWeight.Medium);SmallLabel(draw.drawDate)}
                        Text(draw.number.value,fontFamily=NumberFont,fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary,letterSpacing=4.sp)
                        Icon(Icons.Outlined.ChevronRight,null,Modifier.padding(start=12.dp).size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant)
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
                Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Text(draw.number.value,Modifier.weight(1f),fontFamily=NumberFont,fontSize=26.sp,letterSpacing=4.sp,color=MaterialTheme.colorScheme.primary);SmallLabel(draw.drawDate)}
                Surface(color=MaterialTheme.colorScheme.surfaceContainerLow,shape=RoundedCornerShape(6.dp)) {
                    Column(Modifier.padding(horizontal=12.dp,vertical=8.dp)){
                        listOf("和值" to a.sum.toString(),"跨度" to a.span.toString(),"奇偶" to a.parityPattern,"大小" to a.sizePattern,"质合" to a.qualityPattern,"和尾" to a.sumTail.toString(),"012路" to a.routePositionPattern,"路数比" to a.routeCountPattern).chunked(2).forEach{pairs->Row(Modifier.fillMaxWidth().height(30.dp),verticalAlignment=Alignment.CenterVertically){pairs.forEach{(label,value)->Row(Modifier.weight(1f)){SmallLabel(label,Modifier.width(48.dp));Text(value,fontSize=13.sp,fontWeight=FontWeight.Medium)}}}}
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
                            Text("遗漏 ${missed}期",fontSize=13.sp,fontWeight=FontWeight.Medium)
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
                    report.plays.forEach{play->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(play.playType.displayName,Modifier.width(64.dp),fontSize=13.sp);SmallLabel("${formatMoney(play.winningCount)} 注",Modifier.weight(1f));Text("${formatMoney(play.prizePerBetYuan)} 元/注",fontSize=13.sp)}}
                    report.payoutSummary?.let{SmallLabel("派奖：$it")}
                    report.prizePoolBalanceFen?.let{SmallLabel("奖池 ${"%.2f".format(it/100.0)} 元")}
                    report.redemptionDeadline?.let{SmallLabel("兑奖截止 $it")}
                }
            }
        }
    }
}
