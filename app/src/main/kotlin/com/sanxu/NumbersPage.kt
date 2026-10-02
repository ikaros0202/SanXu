package com.sanxu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.pick.PickEngine
import com.sanxu.ui.*

@Composable fun NumbersPage(model:WorkspaceViewModel) {
    val state by model.editor.collectAsStateWithLifecycle()
    val input=state.draft.input
    val base=remember(input){PickEngine.generate(input.copy(excluded=emptySet())).numbers}
    val selected=remember(input){PickEngine.generate(input).numbers.toSet()}
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp)) {
        WorkspacePanel {
            Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(start=16.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text("保留 ${selected.size} / ${base.size} 个",style=MaterialTheme.typography.titleMedium);SmallLabel("点选排除，再次点选恢复")}
                TextButton(onClick={model.updateInput{it.copy(excluded=emptySet())}},enabled=input.excluded.isNotEmpty()){Text("全部恢复")}
            }
        }
        if(base.isEmpty())EmptyPanel("还没有完整号码","返回选号，先完成数字或条件选择。")
        else LazyVerticalGrid(GridCells.Adaptive(56.dp),Modifier.fillMaxSize().testTag("numbers_grid"),contentPadding=PaddingValues(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            items(base,key={it}){number->
                val active=number in selected
                Surface(onClick={model.updateInput{it.copy(excluded=if(number in it.excluded)it.excluded-number else it.excluded+number)}},modifier=Modifier.height(48.dp).testTag("candidate_$number").semantics{this.selected=active},shape=RoundedCornerShape(8.dp),color=if(active)MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer,border=BorderStroke(1.dp,if(active)MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.surfaceContainer)){
                    Box(contentAlignment=Alignment.Center){Text(number,fontFamily=NumberFont,fontSize=16.sp,fontWeight=if(active)FontWeight.SemiBold else FontWeight.Normal,textDecoration=if(active)TextDecoration.None else TextDecoration.LineThrough,color=if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant);if(active)Icon(Icons.Outlined.Check,null,Modifier.align(Alignment.TopEnd).padding(3.dp).size(10.dp),tint=MaterialTheme.colorScheme.primary)}
                }
            }
        }
    }
}
