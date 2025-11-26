<footer class="site-footer">
    <p>&copy; <span id="year"></span> Micro · 轻量社交平台</p>
    <p class="muted">构建版本：${pageContext.request.serverName}</p>
</footer>
<script>
    document.getElementById('year').textContent = new Date().getFullYear();
</script>
