<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'

const route = useRoute()

const showNavBar = computed(() => {
  return route.path !== '/'
})
</script>

<template>
  <div class="app-container">
    <NavBar v-if="showNavBar" />
    <main class="main-content" :class="{ 'with-nav': showNavBar }">
      <router-view />
    </main>
  </div>
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.main-content.with-nav {
  height: calc(100vh - 64px);
  overflow: auto;
}
</style>
