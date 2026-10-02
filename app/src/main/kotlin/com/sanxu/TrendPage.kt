package com.sanxu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.feature.trend.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun TrendPage(model:WorkspaceViewModel,onPick:()->Unit) {
    val draws by model.draws.collectAsStateWithLifecycle()
    val trials by model.trials.collectAsStateWithLifecycle()
    var window by rememberSaveable { mutableIntStateOf(30) }
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    var menu by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<TrendPoint?>(null) }
    var state by remember { mutableStateOf(TrendUiState()) }
    LaunchedEffect(draws,trials,window) { state=withContext(Dispatchers.Default){buildTrendState(draws,window,TrendPosition.entries.toSet(),trialNumbers=trials)} }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
            Box {
                TextButton(onClick={menu=true}){Text("近 $window 期");Icon(Icons.Outlined.KeyboardArrowDown,null)}
                DropdownMenu(menu,{menu=false}) { listOf(10,30,60,100).forEach { value->DropdownMenuItem(text={Text("近 $value 期")},onClick={window=value;menu=false;selected=null}) } }
            }
            Spacer(Modifier.weight(1f));SmallLabel("双指缩放 · 拖动查看")
        }
        if(draws.isEmpty()) EmptyPanel("暂无开奖记录","更新开奖后即可查看走势。","重试",{model.refresh()})
        else if(state.tableRows.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){CircularProgressIndicator()}
        else TrendChart(state.copy(scale=scale,selectedPoint=selected),accessibilitySummary="福彩3D 近 $window 期走势图，包含百十个位、和值、遗漏与试机号",onSetScale={scale=it},onSelectPoint={selected=it},modifier=Modifier.weight(1f).testTag("trend_ready"))
        selected?.let { point->
            Surface(color=MaterialTheme.colorScheme.secondaryContainer) { Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
                Text("${point.issue} 期 · ${listOf("百位","十位","个位")[point.position.ordinal]} ${point.digit}",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={model.useTrendDigit(point.position.ordinal,point.digit);onPick()}){Text("用于位选")}
                TextButton(onClick={selected=null}){Text("收起")}
            } }
        }
    }
}
