package com.sanxu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanxu.ui.*

fun Set<Int>.toggle(digit:Int)=if(digit in this)this-digit else this+digit
@Composable fun DigitGrid(selected:Set<Int>,tag:String,modifier:Modifier=Modifier,onDigit:(Int)->Unit) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        repeat(2){row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            repeat(5){column->val digit=row*5+column;val active=digit in selected
                Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {
                Surface(onClick={onDigit(digit)},modifier=Modifier.size(48.dp).testTag("${tag}_$digit").semantics{this.selected=active;contentDescription="$digit"},shape=RoundedCornerShape(6.dp),
                    color=if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor=if(active)MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border=BorderStroke(if(active)1.dp else .5.dp,if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                    Box(contentAlignment=Alignment.Center){Text(digit.toString(),fontFamily=NumberFont,fontWeight=if(active)FontWeight.Bold else FontWeight.Medium,fontSize=16.sp);if(active)Icon(Icons.Outlined.Check,null,Modifier.align(Alignment.BottomEnd).padding(5.dp).size(9.dp))}
                }
                }
            }
        }}
    }
}
@Composable fun PoolTools(selected:Set<Int>,onChange:(Set<Int>)->Unit) {
    var more by remember{mutableStateOf(false)}
    Row(Modifier.fillMaxWidth().height(48.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("数字池",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.width(8.dp));SmallLabel("已选 ${selected.size} 个",Modifier.weight(1f))
        TextButton(onClick={onChange((0..9).toSet())},contentPadding=PaddingValues(horizontal=8.dp)){Text("全选")}
        Box { IconButton(onClick={more=true}){Icon(Icons.Outlined.MoreHoriz,"数字池操作")}
            DropdownMenu(more,{more=false}){DropdownMenuItem(text={Text("反选数字")},onClick={onChange((0..9).toSet()-selected);more=false});DropdownMenuItem(text={Text("清空数字")},onClick={onChange(emptySet());more=false})}
        }
    }
}
