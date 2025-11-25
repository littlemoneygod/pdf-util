<template>
  <div id="app">
    <header class="app-header">
      <h1>PDF 工具</h1>
    </header>
    <main class="app-main">
      <PdfViewer />
    </main>
  </div>
</template>

<script>
import PdfViewer from './components/PdfViewer.vue'

export default {
  name: 'App',
  components: {
    PdfViewer
  }
}
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #2c3e50;
  color: white;
  padding: 1rem;
  text-align: center;
}

.app-main {
  flex: 1;
  padding: 2rem;
  background: #ecf0f1;
}
</style>