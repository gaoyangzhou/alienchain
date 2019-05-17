<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="keywords" contect="alienchain, alienchain explorer">
    <meta name="description" contect="alienchain is an innovative high-performance blockchain platform, powered by alienchain BFT consensus algorithm.">
    <title>alienchain explorer</title>

    <link rel="stylesheet" href="${rc.contextPath}/css/element-ui.css">
    <style>
        .el-row {
            margin-bottom: 20px;
        &:last-child {
             margin-bottom: 0;
         }
        }
        .el-col {
            border-radius: 4px;
        }
        .bg-purple-dark {
            background: #99a9bf;
        }
        .bg-purple {
            background: #d3dce6;
        }
        .bg-purple-light {
            background: #e5e9f2;
        }
        .grid-content {
            border-radius: 4px;
            min-height: 36px;
        }
        .row-bg {
            padding: 10px 0;
            background-color: #f9fafc;
        }
    </style>
</head>
<body>
<div id="app">
<el-row :gutter="20">
    <el-col :span="4"><div class="grid-content bg-purple">ALIENCHAIN</div></el-col>
    <el-col :span="6"><div class="grid-content bg-purple">EXPLORER</div></el-col>
    <el-col :span="6"><div class="grid-content bg-purple">DELEGATER</div></el-col>
    <el-col :span="6"><div class="grid-content bg-purple">FAQ</div></el-col>
</el-row>
</div>
</body>
<script src="${rc.contextPath}/js/vue-2.6.10.js"></script>
<script src="${rc.contextPath}/js/element-ui.js"></script>
<script>
    new Vue({
        el: '#app',
        data: function() {
            return { visible: false }
        }
    });

    new Vue().$mount('#app');
</script>
</html>