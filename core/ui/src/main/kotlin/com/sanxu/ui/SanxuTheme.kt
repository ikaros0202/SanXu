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

// secondaryContainer is the identity band; primaryContainer is the editing tint.
private val Light=lightColorScheme(
    primary=Color(0xFFAD243B),onPrimary=Color.White,primaryContainer=Color(0xFFF4E3E6),onPrimaryContainer=Color(0xFF762035),
    secondary=Color(0xFF61565A),onSecondary=Color.White,secondaryContainer=Color(0xFF97243A),onSecondaryContainer=Color.White,
    tertiary=Color(0xFF805221),background=Color(0xFFF1EFEC),onBackground=Color(0xFF292427),surface=Color(0xFFFFFEFD),onSurface=Color(0xFF292427),
    surfaceVariant=Color(0xFFECE7E5),onSurfaceVariant=Color(0xFF696063),outline=Color(0xFF96898D),outlineVariant=Color(0xFFDDD4D5),
    surfaceContainer=Color(0xFFECE7E5),surfaceContainerLow=Color(0xFFF8F5F3),surfaceContainerHigh=Color(0xFFE6DDDE),error=Color(0xFFAA2530))
private val Dark=darkColorScheme(
    primary=Color(0xFFFFACBA),onPrimary=Color(0xFF650C25),primaryContainer=Color(0xFF472630),onPrimaryContainer=Color(0xFFFFD6DE),
    secondary=Color(0xFFD0C0C5),onSecondary=Color(0xFF382B30),secondaryContainer=Color(0xFF6F2334),onSecondaryContainer=Color(0xFFFFE8ED),
    tertiary=Color(0xFFDFC099),background=Color(0xFF171416),onBackground=Color(0xFFF1E8EA),surface=Color(0xFF231E21),onSurface=Color(0xFFF1E8EA),
    surfaceVariant=Color(0xFF342B30),onSurfaceVariant=Color(0xFFCBBCC2),outline=Color(0xFF9C8990),outlineVariant=Color(0xFF4B3C43),
    surfaceContainer=Color(0xFF30272C),surfaceContainerLow=Color(0xFF1D181B),surfaceContainerHigh=Color(0xFF3C3037),error=Color(0xFFFFB4AD))
val NumberFont=FontFamily.Monospace
@Composable fun SanxuTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable ()->Unit) {
    MaterialTheme(colorScheme=if(dark) Dark else Light,shapes=Shapes(small=RoundedCornerShape(8.dp),medium=RoundedCornerShape(12.dp),large=RoundedCornerShape(20.dp)),typography=Typography(
        headlineLarge=TextStyle(fontSize=28.sp,fontWeight=FontWeight.SemiBold),headlineMedium=TextStyle(fontSize=20.sp,lineHeight=26.sp,fontWeight=FontWeight.SemiBold),
        titleLarge=TextStyle(fontSize=20.sp,fontWeight=FontWeight.SemiBold),titleMedium=TextStyle(fontSize=16.sp,fontWeight=FontWeight.SemiBold),
        bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),bodySmall=TextStyle(fontSize=12.sp,lineHeight=18.sp),labelLarge=TextStyle(fontSize=14.sp,fontWeight=FontWeight.Medium)
    ),content=content)
}
@Composable fun SectionLabel(title:String,modifier:Modifier=Modifier){Text(title,modifier.padding(top=16.dp,bottom=10.dp),style=MaterialTheme.typography.titleMedium)}
@Composable fun PaperPanel(modifier:Modifier=Modifier,emphasized:Boolean=false,content:@Composable ColumnScope.()->Unit){Surface(modifier,shape=MaterialTheme.shapes.medium,color=if(emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface){Column(Modifier.padding(12.dp),content=content)}}
