use tiancaiapi;
-- 接口信息表
create table if not exists tiancaiapi.`interface_info`
(
    `id`              bigint                             not null auto_increment comment '接口id' primary key,
    `name`            varchar(256)                       not null comment '接口名',
    `interfaceAvatar` varchar(256)                       null comment '接口头像',
    `description`     varchar(256)                       null comment '接口描述',
    `url`             varchar(256)                       not null comment '接口地址',
    `requestHeader`   text                               null comment '请求头',
    `responseHeader`  text                               null comment '响应头',
    `returnFormat`    text                               null comment '返回格式（如json）',
    `requestParams`   text                               null comment '请求参数',
    `responseParams`  text                               null comment '响应参数',
    `requestExample`  text                               null comment '请求参数示例',
    `reduceScore`     int                                null comment '扣除积分数',
    `status`          int      default 0                 not null comment '接口状态(0关闭1开启)',
    `userId`          bigint                             not null comment '创建人',
    `totalInvokes`    bigint   default 0                 not null comment '总调用次数',
    `method`          varchar(256)                       not null comment '请求类型',
    `createTime`      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`       tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口信息表';

-- 用户调用接口的关系
create table if not exists tiancaiapi.`user_interface_info`
(
    `id`          bigint                             not null auto_increment comment '接口id' primary key,
    `userId`      bigint                             not null comment '用户id',
    `interfaceId` bigint                             not null comment '接口id',
    `totalNum`    int      default 0                 not null comment '调用次数',
    `leftNum`     int      default 0                 not null comment '剩余调用次数',
    `status`      int      default 0                 not null comment '状态(0正常1禁用)',
    `createTime`  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`   tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '用户于调用接口表';


-- 用户表
create table if not exists user
(
    id             bigint auto_increment comment 'id' primary key,
    userAccount    varchar(256)                           not null comment '账号',
    userPassword   varchar(512)                           not null comment '密码',
    userName       varchar(256)                           null comment '用户昵称',
    userAvatar     varchar(1024)                          null comment '用户头像',
    email          varchar(256)                           null comment '邮箱',
    balance        int          default 100               not null comment '余额',
    invitationCode varchar(256)                           not null comment '邀请码',
    accessKey      varchar(512)                           not null comment 'accessKey',
    secretKey      varchar(512)                           not null comment 'secretKey',
    userRole       varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;


-- 商品信息表
create table if not exists tiancaiapi.`product_info`
(
    `id`           bigint                                 not null auto_increment comment '商品id' primary key,
    `userId`       bigint                                 not null comment '用户id',
    `productName`  varchar(256)                           not null comment '商品名',
    `description`  varchar(256)                           null comment '商品描述',
    `price`        double       default 0.0               not null comment '价格',
    `status`       int          default 0                 not null comment '状态(0下线1上线)',
    `productType ` varchar(256) default 'RECHARGE'        not null comment '产品类型（VIP-会员 RECHARGE-充值,RECHARGEACTIVITY-充值活动）',
    `addCoin`      int          default 0                 not null comment '增加甜菜币数',
    `createTime`   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`    tinyint      default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '商品信息表';


-- 每日签到表
create table if not exists tiancaiapi.`daily_sign_in`
(
    `id`          bigint                             not null auto_increment comment '商品id' primary key,
    `userId`      bigint                             not null comment '用户id',
    `description` varchar(256)                       null comment '描述',
    `addCoin`     int      default 10                not null comment '增加甜菜币数',
    `createTime`  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`   tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '每日签到表';

-- 商品订单表
create table if not exists tiancaiapi.`product_order`
(
    `id`             bigint                                 not null auto_increment comment '商品id' primary key,
    `userId`         bigint                                 not null comment '用户id',
    `productId`      bigint                                 not null comment '用户id',
    `orderNo`        varchar(256)                           not null comment '订单号',
    `orderName`      varchar(256)                           not null comment '商品名',
    `total`          bigint                                 not null comment '总金额',
    `status`         varchar(256) default 'NOTPAY'          not null comment '交易状态(SUCCESS：支付成功 REFUND：转入退款 NOTPAY：未支付 CLOSED：已关闭 REVOKED：
                                                                            已撤销（仅付款码支付会返回) USERPAYING：用户支付中（仅付款码支付会返回）PAYERROR：支付失败（仅付款码支付会返回）)',
    `payType`        varchar(256) default 'ZFB'             not null comment '支付方式(ZFB 支付宝)',
    `productInfo`    text                                   null comment '商品信息',
    `fromData`       text                                   null comment '支付宝formData',
    `addPoints`      int                                    not null comment '增加甜菜币数',
    `expirationTime` datetime                               null comment '过期时间',
    `createTime`     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`      tinyint      default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '商品订单表';


#付款信息
create table if not exists tiancaiapi.payment_info
(
    id             bigint auto_increment comment 'id' primary key,
    orderNo        varchar(256)                           null comment '商户订单号',
    tradeType      varchar(256)                           null comment '交易类型',
    transactionId  varchar(256)                           null comment '微信支付订单号',
    tradeState     varchar(256)                           null comment '交易状态(SUCCESS：支付成功 REFUND：转入退款 NOTPAY：未支付 CLOSED：已关闭 REVOKED：已撤销（仅付款码支付会返回）
                                                                              USERPAYING：用户支付中（仅付款码支付会返回）PAYERROR：支付失败（仅付款码支付会返回）)',
    tradeStateDesc varchar(256)                           null comment '交易状态描述',
    successTime    varchar(256)                           null comment '支付完成时间',
    openid         varchar(256)                           null comment '用户标识',
    payerTotal     bigint                                 null comment '用户支付金额',
    currency       varchar(256) default 'CNY'             null comment '货币类型',
    payerCurrency  varchar(256) default 'CNY'             null comment '用户支付币种',
    content        text                                   null comment '接口返回内容',
    total          bigint                                 null comment '总金额(分)',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '付款信息';


-- 充值记录表
create table if not exists tiancaiapi.recharge_record
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '用户id',
    productId  bigint                             not null comment '商品id',
    orderNo        varchar(256)                           null comment '商户订单号',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '充值记录表';