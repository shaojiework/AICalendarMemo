<script>
	import { getToken } from '@/utils/auth'

	export default {
		onLaunch: function() {
			// 登录守卫：应用启动时无token则跳转登录页
			if (!getToken()) {
				uni.reLaunch({ url: '/pages/login/login' })
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
/* 全局引入阿里图标字体 */
@font-face {
  font-family: "iconfont"; 
  src: url("/static/iconfont/iconfont.ttf") format("truetype");
}

</style>
