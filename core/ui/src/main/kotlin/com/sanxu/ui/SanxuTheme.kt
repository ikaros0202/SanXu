package com.sanxu.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val Light=lightColorScheme(primary=Color(0xFF345A87),onPrimary=Color.White,primaryContainer=Color(0xFFE5EDF8),onPrimaryContainer=Color(0xFF233F62),secondary=Color(0xFF596C82),onSecondary=Color.White,secondaryContainer=Color(0xFFE9EEF4),onSecondaryContainer=Color(0xFF2E4158),tertiary=Color(0xFF9A613B),background=Color(0xFFF3F5F8),onBackground=Color(0xFF1B293C),surface=Color.White,onSurface=Color(0xFF1B293C),surfaceVariant=Color(0xFFEDF1F6),onSurfaceVariant=Color(0xFF5D6B7D),outline=Color(0xFF8491A2),outlineVariant=Color(0xFFDCE3EC),surfaceContainer=Color(0xFFEDF1F6),surfaceContainerLow=Color(0xFFF7F9FB),surfaceContainerHigh=Color(0xFFE3E9F1),error=Color(0xFFAF3435))
private val Dark=darkColorScheme(primary=Color(0xFFB4CDF2),onPrimary=Color(0xFF173455),primaryContainer=Color(0xFF29415F),onPrimaryContainer=Color(0xFFD7E6FC),secondary=Color(0xFFBDCBDF),onSecondary=Color(0xFF25354B),secondaryContainer=Color(0xFF27364A),onSecondaryContainer=Color(0xFFDDE6F4),tertiary=Color(0xFFE1BA97),background=Color(0xFF101722),onBackground=Color(0xFFE7EDF7),surface=Color(0xFF192331),onSurface=Color(0xFFE7EDF7),surfaceVariant=Color(0xFF222E40),onSurfaceVariant=Color(0xFFB0BED0),outline=Color(0xFF7E91A9),outlineVariant=Color(0xFF344357),surfaceContainer=Color(0xFF222E40),surfaceContainerLow=Color(0xFF141D2A),surfaceContainerHigh=Color(0xFF2C3A4D))
val NumberFont=FontFamily.Monospace
@Composable fun SanxuTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable ()->Unit) {
    MaterialTheme(colorScheme=if(dark) Dark else Light,shapes=Shapes(small=RoundedCornerShape(8.dp),medium=RoundedCornerShape(18.dp),large=RoundedCornerShape(24.dp)),typography=Typography(
        headlineLarge=TextStyle(fontSize=32.sp,fontWeight=FontWeight.SemiBold),headlineMedium=TextStyle(fontSize=22.sp,lineHeight=28.sp,fontWeight=FontWeight.SemiBold),
        titleLarge=TextStyle(fontSize=20.sp,fontWeight=FontWeight.SemiBold),titleMedium=TextStyle(fontSize=16.sp,fontWeight=FontWeight.SemiBold),
        bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),bodySmall=TextStyle(fontSize=12.sp,lineHeight=18.sp),labelLarge=TextStyle(fontSize=14.sp,fontWeight=FontWeight.Medium)
    ),content=content)
}
@Composable fun SectionLabel(title:String,modifier:Modifier=Modifier){Text(title,modifier.padding(top=16.dp,bottom=10.dp),style=MaterialTheme.typography.titleMedium)}
@Composable fun PaperPanel(modifier:Modifier=Modifier,emphasized:Boolean=false,content:@Composable ColumnScope.()->Unit){Surface(modifier,shape=MaterialTheme.shapes.medium,color=if(emphasized) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface){Column(Modifier.padding(16.dp),content=content)}}
