SET NAMES utf8mb4;

USE learning_auth;
INSERT INTO auth_user (id, tenant_id, username, password_hash, role_code, status, deleted) VALUES
(1002, 0, 'student02', 'student123', 'STUDENT', 1, 0),
(1003, 0, 'student03', 'student123', 'STUDENT', 1, 0),
(1004, 0, 'student04', 'student123', 'STUDENT', 1, 0),
(1005, 0, 'student05', 'student123', 'STUDENT', 1, 0),
(1006, 0, 'student06', 'student123', 'STUDENT', 1, 0),
(1007, 0, 'student07', 'student123', 'STUDENT', 1, 0)
ON DUPLICATE KEY UPDATE
password_hash = VALUES(password_hash),
role_code = VALUES(role_code),
status = VALUES(status),
deleted = VALUES(deleted);

USE learning_user;
INSERT INTO user (id, tenant_id, username, real_name, email, phone, role_code, status, deleted) VALUES
(3, 0, 'student01', '张小明', 'student01@yincai.test', '13800001001', 'STUDENT', 1, 0),
(1002, 0, 'student02', '李雨晴', 'student02@yincai.test', '13800001002', 'STUDENT', 1, 0),
(1003, 0, 'student03', '王浩然', 'student03@yincai.test', '13800001003', 'STUDENT', 1, 0),
(1004, 0, 'student04', '陈思源', 'student04@yincai.test', '13800001004', 'STUDENT', 1, 0),
(1005, 0, 'student05', '刘安琪', 'student05@yincai.test', '13800001005', 'STUDENT', 1, 0),
(1006, 0, 'student06', '赵一诺', 'student06@yincai.test', '13800001006', 'STUDENT', 1, 0),
(1007, 0, 'student07', '周子涵', 'student07@yincai.test', '13800001007', 'STUDENT', 1, 0)
ON DUPLICATE KEY UPDATE
real_name = VALUES(real_name),
email = VALUES(email),
phone = VALUES(phone),
role_code = VALUES(role_code),
status = VALUES(status),
deleted = VALUES(deleted);

USE learning_course;
DELETE FROM knowledge_relation WHERE id BETWEEN 31001 AND 31099;
DELETE FROM knowledge_point WHERE id BETWEEN 21001 AND 26099;
DELETE FROM chapter WHERE id BETWEEN 11001 AND 16099;
DELETE FROM course WHERE id BETWEEN 10001 AND 10099;
INSERT INTO course (id, tenant_id, name, description, cover_image, status, deleted) VALUES
(10001, 0, '数据结构与算法', '面向计算机专业核心基础，覆盖线性表、树、图、查找排序、复杂度分析与算法设计思想。', '/assets/course/data-structure.png', 'PUBLISHED', 0),
(10002, 0, '计算机组成原理', '围绕计算机硬件系统结构，讲解数据表示、运算器、存储系统、指令系统和流水线基础。', '/assets/course/computer-organization.png', 'PUBLISHED', 0),
(10003, 0, '操作系统原理', '覆盖进程线程、调度、同步互斥、内存管理、文件系统和 I/O 管理等核心机制。', '/assets/course/operating-system.png', 'PUBLISHED', 0),
(10004, 0, '数据库系统原理', '学习关系模型、SQL、索引、事务、并发控制、数据库设计和查询优化基础。', '/assets/course/database-system.png', 'PUBLISHED', 0),
(10005, 0, '计算机网络', '覆盖网络体系结构、TCP/IP、路由、可靠传输、HTTP、DNS 与网络安全基础。', '/assets/course/computer-network.png', 'PUBLISHED', 0),
(10006, 0, 'Java 后端工程实践', '以 Spring Boot 为主线，训练 REST API、MyBatis、认证鉴权、工程分层和后端项目交付能力。', '/assets/course/java-backend.png', 'PUBLISHED', 0)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description),
cover_image = VALUES(cover_image),
status = VALUES(status),
deleted = VALUES(deleted);

INSERT INTO chapter (id, course_id, parent_id, name, sort_order, description, deleted) VALUES
(11001, 10001, 0, '线性表、栈与队列', 1, '掌握顺序表、链表、栈、队列和典型应用。', 0),
(11002, 10001, 0, '树、图与经典算法', 2, '学习二叉树、图遍历、最短路径、排序与查找算法。', 0),
(12001, 10002, 0, '数据表示与运算部件', 1, '理解二进制、补码、浮点数和 ALU 运算。', 0),
(12002, 10002, 0, '存储系统与指令流水线', 2, '理解 Cache、主存、指令系统和流水线性能。', 0),
(13001, 10003, 0, '进程线程与调度', 1, '掌握进程模型、线程、上下文切换和 CPU 调度。', 0),
(13002, 10003, 0, '同步、内存与文件系统', 2, '理解同步互斥、死锁、虚拟内存和文件系统。', 0),
(14001, 10004, 0, '关系模型与 SQL', 1, '掌握关系代数、SQL 查询、约束和范式设计。', 0),
(14002, 10004, 0, '索引、事务与并发控制', 2, '学习 B+Tree、事务 ACID、锁和 MVCC。', 0),
(15001, 10005, 0, 'TCP/IP 与传输层', 1, '理解分层模型、IP、TCP、UDP 和可靠传输。', 0),
(15002, 10005, 0, '应用层协议与网络安全', 2, '掌握 HTTP、DNS、HTTPS 和基础安全机制。', 0),
(16001, 10006, 0, 'Spring Boot 与 REST API', 1, '构建分层后端项目、Controller、DTO 和统一响应。', 0),
(16002, 10006, 0, '持久化与认证鉴权', 2, '使用 MyBatis、JWT、RBAC 和工程化测试完成后端闭环。', 0)
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
parent_id = VALUES(parent_id),
name = VALUES(name),
sort_order = VALUES(sort_order),
description = VALUES(description),
deleted = VALUES(deleted);

INSERT INTO knowledge_point (id, course_id, chapter_id, name, description, difficulty, tags, vector_id, deleted) VALUES
(21001, 10001, 11001, '链表插入与删除', '理解单链表节点操作、边界条件和时间复杂度。', 'MEDIUM', '数据结构,链表,复杂度', NULL, 0),
(21002, 10001, 11001, '栈和队列的应用', '掌握表达式求值、括号匹配、BFS 队列等典型场景。', 'MEDIUM', '数据结构,栈,队列', NULL, 0),
(21003, 10001, 11002, '图遍历与最短路径', '掌握 DFS、BFS、Dijkstra 算法的思想和实现。', 'HARD', '算法,图,最短路径', NULL, 0),
(22001, 10002, 12001, '补码与溢出判断', '理解有符号整数编码、补码运算和溢出条件。', 'MEDIUM', '组成原理,补码,运算', NULL, 0),
(22002, 10002, 12002, 'Cache 映射与命中率', '理解直接映射、组相联、全相联和命中率计算。', 'HARD', '组成原理,Cache,存储系统', NULL, 0),
(23001, 10003, 13001, '进程状态转换', '掌握运行、就绪、阻塞状态及调度触发条件。', 'EASY', '操作系统,进程,调度', NULL, 0),
(23002, 10003, 13002, '死锁条件与处理', '理解死锁四个必要条件、预防、避免和检测。', 'HARD', '操作系统,死锁,同步', NULL, 0),
(24001, 10004, 14001, 'SQL 多表查询', '掌握连接查询、子查询、聚合和分组统计。', 'MEDIUM', '数据库,SQL,查询', NULL, 0),
(24002, 10004, 14002, '事务隔离级别', '理解脏读、不可重复读、幻读和隔离级别差异。', 'HARD', '数据库,事务,MVCC', NULL, 0),
(25001, 10005, 15001, 'TCP 三次握手与四次挥手', '理解连接建立、释放过程及状态变化。', 'MEDIUM', '计算机网络,TCP,传输层', NULL, 0),
(25002, 10005, 15002, 'HTTP 请求响应模型', '理解状态码、请求方法、Header、缓存和 HTTPS 基础。', 'MEDIUM', '计算机网络,HTTP,应用层', NULL, 0),
(26001, 10006, 16001, 'REST API 设计', '掌握资源路径、HTTP 方法、状态码和统一响应结构。', 'EASY', 'Java,Spring Boot,REST', NULL, 0),
(26002, 10006, 16002, 'JWT 登录鉴权', '理解 Token 签发、解析、过滤器和权限上下文。', 'MEDIUM', 'Java,JWT,安全', NULL, 0)
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
chapter_id = VALUES(chapter_id),
name = VALUES(name),
description = VALUES(description),
difficulty = VALUES(difficulty),
tags = VALUES(tags),
deleted = VALUES(deleted);

INSERT INTO knowledge_relation (id, point_id, related_point_id, relation_type, deleted) VALUES
(31001, 21001, 21002, 'RELATED', 0),
(31002, 21002, 21003, 'PREREQUISITE', 0),
(31003, 22001, 22002, 'PREREQUISITE', 0),
(31004, 23001, 23002, 'RELATED', 0),
(31005, 24001, 24002, 'PREREQUISITE', 0),
(31006, 25001, 25002, 'PREREQUISITE', 0),
(31007, 26001, 26002, 'PREREQUISITE', 0)
ON DUPLICATE KEY UPDATE
point_id = VALUES(point_id),
related_point_id = VALUES(related_point_id),
relation_type = VALUES(relation_type),
deleted = VALUES(deleted);
