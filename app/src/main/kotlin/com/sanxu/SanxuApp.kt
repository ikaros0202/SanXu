package com.sanxu

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

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
                Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={
                    NavigationBar(containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp) {
                        val names=listOf("首页","走势","选号","方案","彩报")
                        val icons=listOf(Icons.Outlined.Home,Icons.Outlined.ShowChart,Icons.Outlined.Dialpad,Icons.Outlined.FolderOpen,Icons.Outlined.Article)
                        names.forEachIndexed { index,name -> NavigationBarItem(selected=tab==index,onClick={ tab=index },icon={Icon(icons[index],null)},label={Text(name)},modifier=Modifier.testTag("nav_$index")) }
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        if(tab!=0) PageHeader(listOf("叁序","走势","选号","方案","彩报")[tab],
                            subtitle=when(tab){1->"每一期开出的轨迹";2->"按你熟悉的方式";3->"按期整理，开奖后核对";else->"彩吧彩报 · 第三版"},
                            action=if(tab==2) "工具" else null,onAction={navigate("tools")})
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
        SnackbarHost(snackbar,Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom=86.dp))
    }
    editor.feedback?.let { feedback ->
        AlertDialog(onDismissRequest=model::dismissFeedback,
            icon={Icon(if(feedback.repeated>0) Icons.Outlined.Layers else Icons.Outlined.CheckCircle,null)},
            title={Text(if(feedback.repeated>0) "重复号码已合并" else if(feedback.previous!=null) "记录已更新" else "已加入方案")},
            text={Text(if(feedback.repeated>0) "有 ${feedback.repeated} 个号码与本期其他记录重合，倍数已相加。每次选号的来源仍保留，可单独编辑或撤销。"
                else "第 ${feedback.entry.issue} 期 · ${feedback.entry.numbers.size} 个号码 × ${feedback.entry.multiplier} 倍\n可以查看方案，或继续创建一条选号记录。")},
            confirmButton={TextButton(onClick={model.dismissFeedback();goPlan()}){Text("查看方案")}},
            dismissButton={Row { TextButton(onClick={model.undo(feedback)}){Text("撤销本次")}; TextButton(onClick={model.dismissFeedback();model.newEntry()}){Text("继续选号")} }}
        )
    }
}

@Composable fun PageHeader(title:String,subtitle:String,action:String?=null,onAction:()->Unit={}) {
    Row(Modifier.fillMaxWidth().padding(start=20.dp,end=12.dp,top=12.dp,bottom=12.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title,style=MaterialTheme.typography.headlineMedium); Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
        if(action!=null) TextButton(onClick=onAction) { Icon(Icons.Outlined.Tune,null,Modifier.size(18.dp)); Spacer(Modifier.width(6.dp));Text(action) }
    }
}
@Composable fun SecondaryHeader(title:String,onBack:()->Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().height(64.dp),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick=onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack,"返回") }
        Text(title,style=MaterialTheme.typography.titleLarge)
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
@Composable fun SmallLabel(text:String,modifier:Modifier=Modifier) { Text(text,modifier,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant) }
