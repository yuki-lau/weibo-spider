/*
SQLyog Ultimate v8.32 
MySQL - 5.5.30 : Database - sina_weibo
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`sina_weibo` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `sina_weibo`;

/*Table structure for table `comment` */

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
  `weiboID` varchar(16) NOT NULL,
  `poster` varchar(64) NOT NULL,
  `content` varchar(4096) NOT NULL,
  `postTime` datetime NOT NULL,
  `fetchTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Table structure for table `follow` */

DROP TABLE IF EXISTS `follow`;

CREATE TABLE `follow` (
  `follower` varchar(16) NOT NULL,
  `followee` varchar(16) NOT NULL,
  `level` tinyint(4) NOT NULL,
  `fetchTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower`,`followee`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Table structure for table `follower` */

DROP TABLE IF EXISTS `follower`;

CREATE TABLE `follower` (
  `follower` varchar(16) NOT NULL,
  `level` tinyint(4) NOT NULL DEFAULT '0',
  `isFetched` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`follower`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Table structure for table `repost` */

DROP TABLE IF EXISTS `repost`;

CREATE TABLE `repost` (
  `weiboID` varchar(16) NOT NULL,
  `poster` varchar(64) NOT NULL,
  `content` varchar(4096) NOT NULL,
  `postTime` datetime NOT NULL,
  `fetchTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `accountID` varchar(16) NOT NULL,
  `nickName` varchar(64) DEFAULT NULL,
  `domain` varchar(64) DEFAULT NULL,
  `vipLevel` int(11) DEFAULT '1',
  `vipType` int(11) DEFAULT '0' COMMENT '1-个人认证；2-企业认证；3-达人',
  `isFetched` tinyint(1) NOT NULL DEFAULT '0',
  `ad` varchar(64) DEFAULT NULL,
  `an` int(11) DEFAULT '0',
  `bi` varchar(512) DEFAULT NULL,
  `ci` varchar(512) DEFAULT NULL,
  `cnt` int(11) DEFAULT NULL,
  `de` varchar(512) DEFAULT NULL,
  `ei` varchar(512) DEFAULT NULL,
  `fn` int(11) DEFAULT '0',
  `iu` varchar(128) DEFAULT NULL,
  `mn` int(11) DEFAULT '0',
  `sx` char(4) DEFAULT NULL,
  `tg` varchar(512) DEFAULT NULL,
  `un` varchar(64) DEFAULT NULL,
  `vi` varchar(512) DEFAULT NULL,
  `wt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`accountID`)
) ENGINE=InnoDB AUTO_INCREMENT=3001 DEFAULT CHARSET=utf8;

/*Table structure for table `weibo` */

DROP TABLE IF EXISTS `weibo`;

CREATE TABLE `weibo` (
  `accountID` varchar(64) NOT NULL,
  `weiboID` varchar(16) NOT NULL,
  `content` varchar(4096) NOT NULL,
  `postTime` datetime NOT NULL,
  `fetchTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isCommentFetched` tinyint(1) NOT NULL DEFAULT '0',
  `isRepostFetched` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`weiboID`),
  KEY `isCommentFetched` (`isCommentFetched`,`isRepostFetched`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
