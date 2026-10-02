package com.sanxu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import com.sanxu.ui.ContextTag

@Serializable data object Workspace: NavKey
@Serializable data class Detail(val page:String,val issue:String=""):NavKey

@Composable fun SanxuApp(model:WorkspaceViewModel) {
    val stack=rememberNavBackStack(Workspace)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val editor by model.editor.collectAsStateWithLifecycle()
    val syncMessage by model.syncMessage.collectAsStateWithLifecycle()
    val snackbar=remember { SnackbarHostState() }
    LaunchedEffect(editor.message) { editor.message?.let { snackbar.showSnackbar(it); model.dismissMessage() } }
    LaunchedEffect(syncMessage) { syncMessage?.let { snackbar.showSnackbar(it); model.dismissSync() } }
    val navigate:(String)->Unit={ stack.add(Detail(it)) }
    val goPick:()->Unit={ while(stack.size>1) stack.removeAt(stack.lastIndex); tab=2 }
    val goPlan:()->Unit={ while(stack.size>1) stack.removeAt(stack.lastIndex); tab=3 }
    Box(Modifier.fillMaxSize()) {
        NavDisplay(backStack=stack,onBack={ if(stack.size>1) stack.removeAt(stack.lastIndex) },entryProvider=entryProvider {
            entry<Workspace> {
                Scaffold(containerColor=MaterialTheme.colorScheme.background,contentWindowInsets=WindowInsets.navigationBars,bottomBar={
                    Surface(color=MaterialTheme.colorScheme.surface) {
                    Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth().height(64.dp).selectableGroup()) {
                        val names=listOf("首页","走势","选号","方案","彩报")
                        val icons=listOf(Icons.Outlined.Home,Icons.Outlined.ShowChart,Icons.Outlined.GridView,Icons.Outlined.FolderOpen,Icons.Outlined.Article)
                        names.forEachIndexed { index,name ->
                            val active=tab==index
                            Column(Modifier.weight(1f).fillMaxHeight().selectable(selected=active,onClick={tab=index},role=Role.Tab).testTag("nav_$index"),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                                Icon(icons[index],null,Modifier.size(23.dp),tint=if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(name,fontSize=12.sp,fontWeight=if(active) FontWeight.SemiBold else FontWeight.Normal,color=if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Box(Modifier.padding(top=3.dp).size(18.dp,2.dp).background(if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,RoundedCornerShape(1.dp)))
                            }
                        }
                    }
                    }
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).windowInsetsTopHeight(WindowInsets.statusBars))
                        if(tab!=1&&tab!=4) PageHeader(listOf("叁序","走势","选号","方案","彩报")[tab],tag=if(tab==0) "福彩3D" else null) {
                            if(tab==0) IconButton(onClick={navigate("settings")}){Icon(Icons.Outlined.Settings,"偏好设置")}
                            if(tab==2) TextButton(onClick={navigate("tools")},colors=ButtonDefaults.textButtonColors(contentColor=LocalContentColor.current)) { Icon(Icons.Outlined.Tune,null,Modifier.size(18.dp));Spacer(Modifier.width(4.dp));Text("工具") }
                        }
                        when(tab) {
                            0->HomePage(model,onSettings={navigate("settings")},onHistory={stack.add(Detail("history",it))},onPick={tab=2},onPlan={tab=3})
                            1->TrendPage(model,onPick={tab=2})
                            2->PickPage(model,onTools={navigate("tools")},onDetails={navigate("numbers")})
                            3->PlansPage(model,onPick=goPick)
                            4->ReportPage(model)
                        }
                    }
                }
            }
            entry<Detail> { detail ->
                Scaffold(containerColor=MaterialTheme.colorScheme.background,topBar={
                    SecondaryHeader(when(detail.page){"tools"->"选号工具";"numbers"->"号码明细";"settings"->"偏好设置";"history"->"开奖档案";"backtest"->"历史回测";else->"叁序"}) { if(stack.size>1) stack.removeAt(stack.lastIndex) }
                }) { padding -> Box(Modifier.fillMaxSize().padding(padding)) {
                    when(detail.page) {
                        "tools"->ToolsPage(model,onPick=goPick,onBacktest={navigate("backtest")})
                        "numbers"->NumbersPage(model)
                        "settings"->SettingsPage(model)
                        "history"->HistoryPage(model,detail.issue)
                        "backtest"->BacktestPage(model)
                    }
                } }
            }
        })
        val snackbarBottom=when {
            (stack.lastOrNull() as? Detail)?.page=="tools"->84.dp
            stack.size>1->16.dp
            tab==2->142.dp
            else->72.dp
        }
        SnackbarHost(snackbar,Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom=snackbarBottom))
    }
    editor.feedback?.let { feedback ->
        WorkspaceSheet(if(feedback.repeated>0) "重复号码已合并" else if(feedback.previous!=null) "记录已更新" else "已加入方案",model::dismissFeedback) {
            Text("第 ${feedback.entry.issue} 期 · ${feedback.entry.numbers.size} 个号码 × ${feedback.entry.multiplier} 倍",Modifier.padding(vertical=8.dp),style=MaterialTheme.typography.titleMedium)
            SmallLabel(if(feedback.repeated>0) "${feedback.repeated} 个重复号码已累加倍数。来源记录仍可单独调整。" else "已保存到本期清单，可继续选号或查看汇总。")
            Row(Modifier.fillMaxWidth().padding(top=16.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                TextButton(onClick={model.undo(feedback)},modifier=Modifier.weight(1f).height(48.dp)){Text("撤销本次")}
                TextButton(onClick={model.dismissFeedback();model.newEntry()},modifier=Modifier.weight(1f).height(48.dp)){Text("继续选号")}
                Button(onClick={model.dismissFeedback();goPlan()},modifier=Modifier.weight(1f).height(48.dp),contentPadding=PaddingValues(horizontal=8.dp)){Text("查看方案")}
            }
        }
    }
}

@Composable fun PageHeader(title:String,tag:String?=null,actions:@Composable RowScope.()->Unit={}) {
    Surface(color=MaterialTheme.colorScheme.secondaryContainer,contentColor=MaterialTheme.colorScheme.onSecondaryContainer) {
    Row(Modifier.fillMaxWidth().height(48.dp).padding(start=16.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,style=MaterialTheme.typography.headlineMedium)
        if(tag!=null){Spacer(Modifier.width(12.dp));Text(tag,fontSize=12.sp,color=LocalContentColor.current.copy(alpha=.85f))}
        Spacer(Modifier.weight(1f));actions()
    }
    }
}
@Composable fun SecondaryHeader(title:String,onBack:()->Unit) {
    Surface(color=MaterialTheme.colorScheme.secondaryContainer,contentColor=MaterialTheme.colorScheme.onSecondaryContainer) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().height(48.dp),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick=onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack,"返回") }
        Text(title,style=MaterialTheme.typography.titleLarge)
    }
    }
}
@Composable fun EmptyPanel(title:String,body:String,action:String?=null,onAction:()->Unit={}) {
    Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.Inbox,null,Modifier.size(36.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title,style=MaterialTheme.typography.titleMedium)
        Text(body,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(action!=null) OutlinedButton(onClick=onAction){Text(action)}
    }
}
@Composable fun SmallLabel(text:String,modifier:Modifier=Modifier) { Text(text,modifier,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
