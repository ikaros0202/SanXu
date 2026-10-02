package com.sanxu

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.livecontent.LiveContentRefreshState
import com.sanxu.domain.repository.CaibaoImageReadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import com.sanxu.ui.WorkspacePanel

@Composable fun ReportPage(model:WorkspaceViewModel) {
    val documents by model.documents.collectAsStateWithLifecycle()
    val draws by model.draws.collectAsStateWithLifecycle()
    val refresh by model.caibaoState.collectAsStateWithLifecycle()
    val today=LocalDate.now(ZoneId.of("Asia/Shanghai"))
    val options=remember(documents,draws,today){
        (documents.filter { it.cachedLocalDate in today.minusDays(29)..today }.map { it.issue }+draws.filter { runCatching { LocalDate.parse(it.drawDate) in today.minusDays(29)..today }.getOrDefault(false) }.map { it.issue }).distinct().sortedDescending()
    }
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val issue=chosen?.takeIf { it in options } ?: options.firstOrNull()
    val document=documents.firstOrNull { it.issue==issue }
    var menu by remember { mutableStateOf(false) }
    var zoomMenu by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var decoding by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }
    var scale by remember(document) { mutableFloatStateOf(1f) }
    var offset by remember(document) { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(Unit){model.live.cleanCaibaoCache();model.openReports()}
    LaunchedEffect(document) {
        bitmap=null;imageError=false;decoding=document!=null
        if(document!=null) {
            val loaded=withContext(Dispatchers.IO) {
                when(val result=model.live.readCaibaoImage(document)) {
                    is CaibaoImageReadResult.Loaded->{
                        val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
                        BitmapFactory.decodeByteArray(result.bytes,0,result.bytes.size,bounds)
                        var sample=1
                        while(bounds.outWidth/sample>4096||bounds.outHeight/sample>4096) sample*=2
                        runCatching { BitmapFactory.decodeByteArray(result.bytes,0,result.bytes.size,BitmapFactory.Options().apply{inSampleSize=sample})?.asImageBitmap() }.getOrNull()
                    }
                    else->null
                }
            }
            bitmap=loaded
            if(loaded==null){imageError=true;model.live.invalidateCaibaoImage(document)}
        }
        decoding=false
    }
    fun choose(value:String){chosen=value;model.refreshReport(value)}
    Column(Modifier.fillMaxSize()) {
        PageHeader("彩报","A11 · 第三版") {
            IconButton(onClick={model.refreshReport(issue)},enabled=refresh!=LiveContentRefreshState.Refreshing){Icon(Icons.Outlined.Refresh,"刷新彩报",Modifier.size(20.dp))}
        }
        WorkspacePanel(Modifier.padding(horizontal=12.dp).padding(bottom=8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            val index=options.indexOf(issue)
            IconButton(onClick={options.getOrNull(index+1)?.let(::choose)},enabled=index>=0&&index<options.lastIndex){Icon(Icons.Outlined.ChevronLeft,"上一期")}
            Box(Modifier.weight(1f)) {
                TextButton(onClick={menu=true},modifier=Modifier.fillMaxWidth(),enabled=options.isNotEmpty()){Text(issue?.let { "第 $it 期" } ?: "最近30日");Icon(Icons.Outlined.KeyboardArrowDown,null)}
                DropdownMenu(menu,{menu=false}){options.forEach { value->DropdownMenuItem(text={Text("第 $value 期")},onClick={choose(value);menu=false}) }}
            }
            IconButton(onClick={options.getOrNull(index-1)?.let(::choose)},enabled=index>0){Icon(Icons.Outlined.ChevronRight,"下一期")}
            VerticalDivider(Modifier.height(20.dp),color=MaterialTheme.colorScheme.outlineVariant)
            Box {
                TextButton(onClick={zoomMenu=true},enabled=bitmap!=null,modifier=Modifier.widthIn(min=80.dp).semantics{contentDescription="缩放控制"}){Text("${(scale*100).toInt()}%");Icon(Icons.Outlined.KeyboardArrowDown,null,Modifier.size(16.dp))}
                DropdownMenu(zoomMenu,{zoomMenu=false}) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        IconButton(onClick={scale=(scale-.5f).coerceAtLeast(1f);offset=Offset.Zero},enabled=scale>1f){Icon(Icons.Outlined.Remove,"缩小")}
                        TextButton(onClick={scale=1f;offset=Offset.Zero}){Text("${(scale*100).toInt()}% · 复位")}
                        IconButton(onClick={scale=(scale+.5f).coerceAtMost(5f)},enabled=scale<5f){Icon(Icons.Outlined.Add,"放大")}
                    }
                }
            }
        }
        }
        if(refresh==LiveContentRefreshState.Refreshing||decoding) LinearProgressIndicator(Modifier.fillMaxWidth())
        if(refresh is LiveContentRefreshState.Failed&&bitmap!=null) SmallLabel("更新未成功，仍可查看已加载的这一期",Modifier.padding(horizontal=12.dp))
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onSizeChanged { size=it },contentAlignment=Alignment.Center) {
            val image=bitmap
            if(image!=null) Image(image,contentDescription="$issue 期 彩吧彩报第三版",contentScale=ContentScale.Fit,
                modifier=Modifier.fillMaxSize().pointerInput(document){detectTransformGestures { _,pan,zoom,_->
                    scale=(scale*zoom).coerceIn(1f,5f)
                    val x=size.width*(scale-1)/2;val y=size.height*(scale-1)/2
                    offset=Offset((offset.x+pan.x).coerceIn(-x,x),(offset.y+pan.y).coerceIn(-y,y))
                }}.graphicsLayer { scaleX=scale;scaleY=scale;translationX=offset.x;translationY=offset.y })
            else if(!decoding&&refresh!=LiveContentRefreshState.Refreshing) EmptyPanel("这一期彩报暂不可用",when {
                imageError->"图片未能完整显示，请重新加载。"
                options.isEmpty()->"暂时没有可查看的期号，请刷新后重试。"
                else->"可刷新，或选择最近30日内的其他期号。"
            },"重试",{model.refreshReport(issue)})
        }
    }
}
