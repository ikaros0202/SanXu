package com.sanxu.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun WorkspacePanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Surface(modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surface) {
        Column(content=content)
    }
}

/** Connected choices form one control, rather than a row of unrelated buttons. */
@Composable fun ChoiceRail(labels:List<String>,selected:Int,onSelect:(Int)->Unit,modifier:Modifier=Modifier,tags:List<String> = emptyList(),enabled:Boolean=true) {
    Surface(modifier,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            labels.forEachIndexed { index,label ->
                val active=index==selected
                Surface(onClick={onSelect(index)},enabled=enabled,
                    modifier=Modifier.weight(1f).heightIn(min=48.dp).then(tags.getOrNull(index)?.let { Modifier.testTag(it) } ?: Modifier)
                        .semantics { this.selected=active;role=Role.Tab },
                    color=MaterialTheme.colorScheme.surface,
                    contentColor=if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) {
                    Box(Modifier.padding(horizontal=4.dp),contentAlignment=Alignment.Center) {
                        Text(label,fontSize=14.sp,fontWeight=if(active) FontWeight.SemiBold else FontWeight.Normal,maxLines=1)
                        if(active) Box(Modifier.align(Alignment.BottomCenter).width(28.dp).height(3.dp).background(MaterialTheme.colorScheme.primary,RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
    }
}

@Composable fun PanelHeading(title:String,modifier:Modifier=Modifier,actions:@Composable RowScope.()->Unit={}) {
    Row(modifier.fillMaxWidth().heightIn(min=48.dp).padding(start=16.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
        actions()
    }
}

@Composable fun ContextTag(text:String,modifier:Modifier=Modifier) {
    Surface(modifier,shape=RoundedCornerShape(6.dp),color=MaterialTheme.colorScheme.primaryContainer,contentColor=MaterialTheme.colorScheme.onPrimaryContainer) {
        Text(text,Modifier.padding(horizontal=8.dp,vertical=4.dp),fontSize=12.sp,fontWeight=FontWeight.Medium)
    }
}

@Composable fun Metric(value:String,label:String,modifier:Modifier=Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(value,fontFamily=NumberFont,fontSize=if(value.length>=7)18.sp else 24.sp,fontWeight=FontWeight.SemiBold,maxLines=1)
        Text(label,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
