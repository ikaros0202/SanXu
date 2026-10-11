package com.sanxu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.repository.ThemeChoice
import com.sanxu.ui.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun SettingsPage(model:WorkspaceViewModel) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val metadata by model.metadata.collectAsStateWithLifecycle()
    val draws by model.draws.collectAsStateWithLifecycle()
    val refreshing by model.refreshing.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        WorkspacePanel {
            PanelHeading("外观")
            ChoiceRail(listOf("跟随系统","浅色","深色"),ThemeChoice.entries.indexOf(settings.theme),{model.setTheme(ThemeChoice.entries[it])},Modifier.padding(start=12.dp,end=12.dp,bottom=12.dp))
        }
        WorkspacePanel {
            Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("首页核对提示",style=MaterialTheme.typography.titleMedium);SmallLabel("已有方案开奖后显示核对入口")};Switch(settings.showReplayNotice,model::setNotice)}
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(start=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text("开奖数据 · ${draws.size} 期",style=MaterialTheme.typography.titleMedium)
                    SmallLabel(metadata?.lastSuccessEpochMillis?.let{"上次更新 ${Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))}"} ?: "尚未完成首次更新")
                }
                if(refreshing)CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp),strokeWidth=2.dp) else IconButton(onClick=model::refresh){Icon(Icons.Outlined.Refresh,"更新开奖数据")}
            }
        }
        WorkspacePanel {
            PanelHeading("叁序"){ContextTag(BuildConfig.VERSION_NAME,Modifier.padding(end=8.dp))}
            Column(Modifier.padding(start=12.dp,end=12.dp,bottom=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                Text("福彩3D 数据分析与个人方案整理",style=MaterialTheme.typography.bodyMedium)
                SmallLabel("历史统计用于观察与复盘，不表示未来中奖概率。")
                SmallLabel("记录保存在这台设备，卸载会删除个人记录。")
            }
        }
    }
}
