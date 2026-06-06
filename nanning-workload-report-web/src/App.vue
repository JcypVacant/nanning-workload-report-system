<!--
  App.vue - 应用根组件
  包含 Vue Router 的视图渲染出口
-->
<template>
  <router-view />
</template>

<script setup lang="ts">
// 应用根组件，仅作为路由视图的容器
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>
