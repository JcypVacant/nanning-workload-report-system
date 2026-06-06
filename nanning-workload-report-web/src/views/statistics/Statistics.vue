<!-- 统计分析页面 - 使用ECharts展示多维度统计数据 -->
<template>
  <div>
    <h2>统计分析</h2>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:24px;font-weight:bold;color:#409eff">{{stats.totalHours||'--'}}</div><div style="color:#999">总工时（分钟）</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:24px;font-weight:bold;color:#67c23a">{{stats.totalPoints||'--'}}</div><div style="color:#999">总工分</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:24px;font-weight:bold;color:#e6a23c">{{stats.submitCount||'--'}}</div><div style="color:#999">已提交条数</div></div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12"><el-card><template #header>车间工时对比</template><div ref="barChartRef" style="height:300px"></div></el-card></el-col>
      <el-col :span="12"><el-card><template #header>项目占比</template><div ref="pieChartRef" style="height:300px"></div></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const stats = ref({totalHours:'12,580',totalPoints:'3,260',submitCount:'45'})
const barChartRef = ref()
const pieChartRef = ref()
let barChart:echarts.ECharts|null=null
let pieChart:echarts.ECharts|null=null

onMounted(()=>{
  if(barChartRef.value){barChart=echarts.init(barChartRef.value);barChart.setOption({tooltip:{},xAxis:{type:'category',data:['贺州','桂林','柳州','南宁']},yAxis:{type:'value'},series:[{data:[3200,2800,1900,2200],type:'bar',itemStyle:{color:'#409eff'}}]})}
  if(pieChartRef.value){pieChart=echarts.init(pieChartRef.value);pieChart.setOption({tooltip:{},series:[{type:'pie',radius:['40%','70%'],data:[{name:'施工',value:3500},{name:'培训',value:1800},{name:'维修',value:2200},{name:'故障',value:1500},{name:'其他',value:2800}]}]})}
})

onUnmounted(()=>{barChart?.dispose();pieChart?.dispose()})
</script>
