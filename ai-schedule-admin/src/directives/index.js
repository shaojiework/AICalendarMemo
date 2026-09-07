/**
 * Vue3 全局自定义指令
 * 提供指令对象，由 main.js 通过 app.use(directives) 注册
 * 用法：
 *   <div v-draggable="{ handleSelector: '.drag-handle' }"> ... </div>  // 指定内部手柄
 *   <div v-draggable> ... </div>                                        // 整个元素作为手柄（如FAB图标）
 */
export default {
  install(app) {
    app.directive('draggable', {
      mounted(el, binding) {
        // 手柄选择器：传入则在 el 内部查找；不传则 el 本身作为手柄
        const handleSelector = binding.value?.handleSelector
        const handle = handleSelector ? el.querySelector(handleSelector) : el
        if (!handle) {
          console.warn('[v-draggable] 未找到拖拽手柄元素：', handleSelector)
          return
        }

        // 关键样式：让被绑定元素脱离文档流并允许 left/top 定位
        if (getComputedStyle(el).position === 'static') {
          el.style.position = 'fixed'
        }
        handle.style.cursor = 'move'

        // 拖拽起始状态
        let startX = 0
        let startY = 0
        let originLeft = 0
        let originTop = 0
        let dragging = false
        // 是否发生过实际位移（用于区分点击与拖拽，避免拖拽FAB后误触发click展开）
        let hasMoved = false
        // 拖拽位移阈值：小于该值视为点击
        const MOVE_THRESHOLD = 5

        const onMouseDown = (e) => {
          if (e.button !== 0) return
          dragging = true
          hasMoved = false
          const rect = el.getBoundingClientRect()
          originLeft = rect.left
          originTop = rect.top
          // 切换为 left/top 定位，清除 right/bottom 避免冲突（FAB初始用 right/bottom 定位）
          el.style.left = rect.left + 'px'
          el.style.top = rect.top + 'px'
          el.style.right = 'auto'
          el.style.bottom = 'auto'
          startX = e.clientX
          startY = e.clientY
          document.body.style.userSelect = 'none'
          el.style.transition = 'none'
          document.addEventListener('mousemove', onMouseMove)
          document.addEventListener('mouseup', onMouseUp)
        }

        const onMouseMove = (e) => {
          if (!dragging) return
          e.preventDefault()
          const dx = e.clientX - startX
          const dy = e.clientY - startY
          // 位移超过阈值才标记为拖拽（避免微小抖动吞掉点击）
          if (Math.abs(dx) > MOVE_THRESHOLD || Math.abs(dy) > MOVE_THRESHOLD) {
            hasMoved = true
          }
          let newLeft = originLeft + dx
          let newTop = originTop + dy
          // 边界限制：不能拖出可视区域
          const maxLeft = window.innerWidth - el.offsetWidth
          const maxTop = window.innerHeight - el.offsetHeight
          newLeft = Math.max(0, Math.min(newLeft, maxLeft))
          newTop = Math.max(0, Math.min(newTop, maxTop))
          el.style.left = newLeft + 'px'
          el.style.top = newTop + 'px'
          el.style.transform = 'none'
        }

        const onMouseUp = () => {
          if (!dragging) return
          dragging = false
          document.body.style.userSelect = ''
          el.style.transition = ''
          document.removeEventListener('mousemove', onMouseMove)
          document.removeEventListener('mouseup', onMouseUp)
          // 拖拽发生过位移时，阻止后续 click 事件触发（避免拖拽FAB后误触展开）
          if (hasMoved) {
            const blockClick = (ev) => {
              ev.stopPropagation()
              ev.preventDefault()
              document.removeEventListener('click', blockClick, true)
            }
            // 在 capture 阶段拦截，先于目标元素的 click 监听执行
            document.addEventListener('click', blockClick, true)
          }
        }

        handle.addEventListener('mousedown', onMouseDown)

        el._dragCleanup = () => {
          handle.removeEventListener('mousedown', onMouseDown)
          document.removeEventListener('mousemove', onMouseMove)
          document.removeEventListener('mouseup', onMouseUp)
        }
      },
      unmounted(el) {
        if (typeof el._dragCleanup === 'function') {
          el._dragCleanup()
          delete el._dragCleanup
        }
      }
    })
  }
}
