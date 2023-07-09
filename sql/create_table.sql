
-- 创建库
create database if not exists flybi_db;

-- 切换库
use flybi_db;

-- chart
create table chart
(
    id          bigint auto_increment comment 'id'
        primary key,
    goal        text                                   null comment '分析目标',
    name        varchar(128)                           null comment '图表名称',
    chartData   text                                   null comment '图表数据',
    chartType   varchar(128)                           null comment '图表类型',
    genChart    text                                   null comment '生成的图表数据',
    genResult   text                                   null comment '生成的分析结论',
    userId      bigint                                 null comment '创建用户 id',
    isDelete    tinyint      default 0                 not null comment '是否删除',
    status      varchar(128) default 'wait'            not null comment 'wait,succeed,failed,running',
    execMessage text                                   null comment '执行信息',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '图表信息表' collate = utf8mb4_unicode_ci;

-- 用户表
create table user
(
    phoneNum     varchar(20)                            null comment '电话',
    email        varchar(100)                           null comment '邮箱',
    leftCount    int          default 100               null comment '剩余次数',
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (unionId);


create table aimodel
(
    id            int auto_increment comment '主键' primary key,
    AIName        varchar(1024) default 'AI'                                                                        null comment '创建ai名字',
    AIDescription varchar(1024) default 'AI'                                                                        null comment '描述AI',
    createTime    datetime      default CURRENT_TIMESTAMP                                                           not null comment '创建时间',
    updateTime    datetime      default CURRENT_TIMESTAMP                                                           not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      int           default 0                                                                           null comment '是否删除',
    AIRoute       varchar(1024) default '/add_chart'                                                                null comment 'AI的路由',
    isOnline      varchar(16)   default 'onLine'                                                                    null comment '是否上线,online/offline',
    AIAvatar      varchar(1024) default 'https://icons.download/icons/medium/outline/round/communication/inbox.svg' null comment 'ai图片链接',
    constraint AIModel_id_uindex
        unique (id)
);

alter table aimodel
    add primary key (id);


