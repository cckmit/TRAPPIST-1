<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
      <el-form-item label="应用名称" prop="clientName">
        <el-input
          v-model="queryParams.clientName"
          placeholder="请输入应用名称"
          clearable
          size="small"
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh-right" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPerm="['clientDetails_add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleEdit"
          v-hasPerm="['clientDetails_edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDel"
          v-hasPerm="['clientDetails_del']"
        >删除
        </el-button>
      </el-col>
      <div class="top-right-btn">
        <el-tooltip class="item" effect="dark" content="刷新" placement="top">
          <el-button size="mini" circle icon="el-icon-refresh" @click="handleQuery"/>
        </el-tooltip>
        <el-tooltip class="item" effect="dark" :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
          <el-button size="mini" circle icon="el-icon-search" @click="showSearch=!showSearch"/>
        </el-tooltip>
      </div>
    </el-row>

    <el-table v-loading="loading" :data="clientDetailsList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="应用标识" prop="clientId"/>
      <el-table-column label="应用名称" prop="clientName"/>
      <el-table-column label="秘钥" prop="clientSecretStr" :show-overflow-tooltip="true"/>
      <el-table-column label="域" prop="scope"/>
      <el-table-column label="授权模式" prop="authorizedGrantTypes"/>
      <el-table-column label="自动放行" prop="autoapprove" :formatter="autoapproveFormat"/>
      <el-table-column label="令牌时效(s)" prop="accessTokenValidity"/>
      <el-table-column label="刷新时效(s)" prop="refreshTokenValidity"/>
      <el-table-column label="支持ID令牌" prop="supportIdToken" :formatter="supportIdTokenFormat"/>
      <el-table-column label="ID时效(s)" prop="idTokenValidity"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
            v-hasPerm="['clientDetails_edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDel(scope.row)"
            v-hasPerm="['clientDetails_del']"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.current"
      :limit.sync="queryParams.size"
      @pagination="getList"
    />

    <!-- 添加或修改终端配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应用标识" prop="clientId">
          <el-input v-model="form.clientId" placeholder="请输入应用标识"/>
        </el-form-item>
        <el-form-item label="应用名称" prop="clientName">
          <el-input v-model="form.clientName" placeholder="请输入应用名称"/>
        </el-form-item>
        <el-form-item label="秘钥" prop="clientSecretStr">
          <el-input v-model="form.clientSecretStr" placeholder="请输入秘钥"/>
        </el-form-item>
        <el-form-item label="域" prop="scope">
          <el-input v-model="form.scope" placeholder="请输入域"/>
        </el-form-item>
        <el-form-item label="授权模式" prop="authorizedGrantTypes">
          <el-input v-model="form.authorizedGrantTypes" placeholder="请输入授权模式"/>
        </el-form-item>
        <el-form-item label="自动授权" prop="autoapprove">
          <el-select v-model="form.autoapprove" placeholder="请选择是否自动授权">
            <el-option
              v-for="dict in sysOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="令牌时效" prop="accessTokenValidity">
          <el-input-number v-model="form.accessTokenValidity" controls-position="right" :min="0"/>
        </el-form-item>
        <el-form-item label="刷新时效" prop="refreshTokenValidity">
          <el-input-number v-model="form.refreshTokenValidity" controls-position="right" :min="0"/>
        </el-form-item>
        <el-form-item label="支持ID令牌" prop="supportIdToken">
          <el-select v-model="form.supportIdToken" placeholder="请选择是否支持ID令牌">
            <el-option
              v-for="dict in sysOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="ID时效(s)" prop="idTokenValidity">
          <el-input-number v-model="form.idTokenValidity" controls-position="right" :min="0"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {
    listClientDetails,
    getClientDetails,
    delClientDetails,
    addClientDetails,
    editClientDetails,
    changeStatus
  } from "@/api/system/clientDetails";

  export default {
    name: "ClientDetails",
    data() {
      return {
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 总条数
        total: 0,
        // 终端表格数据
        clientDetailsList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          current: 1,
          size: 10,
          clientId: undefined
        },
        // 是否新增
        isAdd: false,
        // 显示搜索条件
        showSearch: true,
        sysOptions: [],
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          clientId: [
            {required: true, message: "应用标识不能为空", trigger: "blur"}
          ],
          clientName: [
            {required: true, message: "应用名称不能为空", trigger: "blur"}
          ],
          clientSecretStr: [
            {required: true, message: "秘钥不能为空", trigger: "blur"}
          ],
          scope: [
            {required: true, message: "域不能为空", trigger: "blur"}
          ],
          authorizedGrantTypes: [
            {required: true, message: "授权模式不能为空", trigger: "blur"}
          ],
          autoapprove: [
            {required: true, message: "自动放行不能为空", trigger: "blur"}
          ]
        }
      };
    },
    created() {
      this.getList();
      this.getDicts("yes_no").then(response => {
        this.sysOptions = response.data;
      });
    },
    methods: {
      /** 查询终端列表 */
      getList() {
        this.loading = true;
        listClientDetails(this.addDateRange(this.queryParams, this.dateRange)).then(
          response => {
            this.clientDetailsList = response.data;
            this.total = response.total;
            this.loading = false;
          }
        );
      },
      supportIdTokenFormat(row, column) {
        return this.selectDictLabel(this.sysOptions, row.supportIdToken);
      },
      autoapproveFormat(row, column) {
        return this.selectDictLabel(this.sysOptions, row.autoapprove);
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        if (this.$refs.menu != undefined) {
          this.$refs.menu.setCheckedKeys([]);
        }
        this.form = {
          clientId: undefined,
          clientName: undefined,
          clientSecretStr: undefined,
          scope: 'server',
          authorizedGrantTypes: 'password,refresh_token',
          accessTokenValidity: undefined,
          refreshTokenValidity: undefined,
          autoapprove: undefined,
          supportIdToken: undefined,
          idTokenValidity: undefined
        };
        this.resetForm("form");
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.current = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm("queryForm");
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        console.log(JSON.stringify(selection))
        this.ids = selection.map(item => item.clientId)
        this.single = selection.length!=1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.isAdd = true;
        this.title = "添加终端";
      },
      /** 修改按钮操作 */
      handleEdit(row) {
        this.reset();
        this.isAdd = false;
        const clientId = row.clientId || this.ids
        getClientDetails(clientId).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改终端";
        });
      },
      /** 提交按钮 */
      submitForm: function () {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (!this.isAdd && this.form.clientId != undefined) {
              editClientDetails(this.form).then(response => {
                if (response.code === 0) {
                  this.msgSuccess("修改成功");
                  this.open = false;
                  this.getList();
                } else {
                  this.msgError(response.msg);
                }
              });
            } else {
              addClientDetails(this.form).then(response => {
                if (response.code === 0) {
                  this.msgSuccess("新增成功");
                  this.open = false;
                  this.getList();
                } else {
                  this.msgError(response.msg);
                }
              });
            }
          }
        });
      },
      /** 删除按钮操作 */
      handleDel(row) {
        const clientId = row.clientId || this.ids;
        this.$confirm('是否确认删除终端应用名称为"' + clientId + '"的数据项?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function () {
          return delClientDetails(clientId);
        }).then(() => {
          this.getList();
          this.msgSuccess("删除成功");
        }).catch(function () {
        });
      }
    }
  };
</script>
