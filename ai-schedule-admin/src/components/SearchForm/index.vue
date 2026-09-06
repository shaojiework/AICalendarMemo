<script setup>
import { reactive } from 'vue'

/**
 * 通用搜索区组件（配置驱动 + 动态组件渲染）
 *
 * fields 配置项：
 * @param {string} label       表单标签
 * @param {string} prop        绑定字段名
 * @param {string} component   组件名：el-input / el-select / el-date-picker 等（全局注册组件均可）
 * @param {string} placeholder 占位文案（可选，默认“请输入/请选择+label”）
 * @param {Array}  options     el-select 选项：[{ label, value }]
 * @param {Object} attrs       透传给动态组件的额外属性（如 el-date-picker 的 type/value-format）
 * @param {string|number} width 输入框宽度，默认 220px
 */
const props = defineProps({
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['search', 'reset'])

// 按 fields 初始化表单对象
const buildInitForm = () => {
  const init = {}
  props.fields.forEach((field) => {
    init[field.prop] = ''
  })
  return init
}

const form = reactive(buildInitForm())

// 点击查询：抛出当前表单条件
const handleSearch = () => {
  emit('search', { ...form })
}

// 点击重置：清空条件后自动触发一次查询
const handleReset = () => {
  Object.assign(form, buildInitForm())
  emit('reset')
  emit('search', { ...form })
}
</script>

<template>
  <!-- 搜索区容器（.search-card 样式由全局统一管理） -->
  <div class="search-card">
    <el-form :model="form" inline label-position="left" label-width="80px">
      <el-form-item v-for="field in fields" :key="field.prop" :label="field.label">
        <component
          :is="field.component"
          v-model="form[field.prop]"
          :placeholder="field.placeholder || (field.component === 'el-select' ? `请选择${field.label}` : `请输入${field.label}`)"
          :style="{ width: (field.width || 220) + 'px' }"
          clearable
          v-bind="field.attrs"
        >
          <!-- el-select 选项动态渲染 -->
          <template v-if="field.component === 'el-select'">
            <el-option
              v-for="opt in field.options || []"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </template>
        </component>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        <el-button icon="RefreshLeft" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
