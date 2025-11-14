-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: barangayhealthdb
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auditlog`
--

DROP TABLE IF EXISTS `auditlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditlog` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `action` varchar(100) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `auditlog_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditlog`
--

LOCK TABLES `auditlog` WRITE;
/*!40000 ALTER TABLE `auditlog` DISABLE KEYS */;
INSERT INTO `auditlog` VALUES (1,1,'LOGIN','2025-11-10 09:44:42'),(2,1,'LOGIN','2025-11-10 09:45:50'),(3,1,'LOGIN','2025-11-10 09:47:17'),(4,1,'LOGIN','2025-11-10 09:48:53'),(5,1,'LOGIN','2025-11-10 09:51:53'),(6,1,'LOGIN','2025-11-10 09:52:14'),(7,1,'UPDATE_RESIDENT: 18','2025-11-10 09:52:27'),(8,1,'UPDATE_RESIDENT: 20','2025-11-10 09:52:42'),(9,1,'LOGIN','2025-11-10 10:00:34'),(10,1,'ADD_RESIDENT: Big Ced','2025-11-10 10:01:07'),(11,1,'ADD_RESIDENT: Hello R','2025-11-10 10:02:43'),(12,1,'LOGIN','2025-11-10 10:03:47'),(13,1,'ADD_RESIDENT: Kim Ver','2025-11-10 10:04:08'),(14,1,'LOGIN','2025-11-10 10:10:27'),(15,1,'LOGIN','2025-11-10 10:11:41'),(16,5,'LOGIN','2025-11-10 10:12:17'),(17,1,'LOGIN','2025-11-10 10:12:53'),(18,1,'LOGIN','2025-11-10 10:17:15'),(19,1,'LOGIN','2025-11-10 10:19:24'),(20,5,'LOGIN','2025-11-10 23:22:33'),(21,1,'LOGIN','2025-11-10 23:22:58'),(22,1,'LOGIN','2025-11-10 23:56:01'),(23,1,'LOGIN','2025-11-10 23:57:28'),(24,1,'LOGIN','2025-11-10 23:59:14'),(25,1,'LOGIN','2025-11-11 00:01:58'),(26,1,'LOGIN','2025-11-11 00:02:57'),(27,1,'LOGIN','2025-11-11 00:06:16'),(28,1,'LOGIN','2025-11-11 00:11:18'),(29,6,'CHANGE_PASSWORD','2025-11-11 00:11:45'),(30,1,'LOGIN','2025-11-11 00:18:59'),(31,1,'DELETE_USER: 9','2025-11-11 00:19:29');
/*!40000 ALTER TABLE `auditlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clinicinventory`
--

DROP TABLE IF EXISTS `clinicinventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clinicinventory` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `item_name` varchar(50) NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `quantity` int DEFAULT '0',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clinicinventory`
--

LOCK TABLES `clinicinventory` WRITE;
/*!40000 ALTER TABLE `clinicinventory` DISABLE KEYS */;
INSERT INTO `clinicinventory` VALUES (1,'Paracetamol','Medicine',200),(2,'Paracetamol 500mg','Medicine',500),(3,'Amoxicillin 500mg','Medicine',300),(4,'Betadine Solution','Medical Supply',50),(5,'Cotton Balls','Medical Supply',200),(6,'Syringe 5ml','Medical Equipment',100),(7,'Face Masks','PPE',1000),(8,'Alcohol 70%','Medical Supply',80),(9,'Bandages','Medical Supply',150),(10,'Thermometer','Medical Equipment',10),(11,'Blood Pressure Monitor','Medical Equipment',5),(12,'Paracetamol 500mg','Medicine',500),(13,'Amoxicillin 500mg','Medicine',300),(14,'Betadine Solution','Medical Supply',50),(15,'Cotton Balls','Medical Supply',200),(16,'Syringe 5ml','Medical Equipment',100),(17,'Face Masks','PPE',1000),(18,'Alcohol 70%','Medical Supply',80),(19,'Bandages','Medical Supply',150),(20,'Thermometer','Medical Equipment',10),(21,'Blood Pressure Monitor','Medical Equipment',5);
/*!40000 ALTER TABLE `clinicinventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `healthpersonnel`
--

DROP TABLE IF EXISTS `healthpersonnel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `healthpersonnel` (
  `personnel_id` int NOT NULL AUTO_INCREMENT,
  `last_name` varchar(50) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  `specialization` varchar(50) DEFAULT NULL,
  `contact_number` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`personnel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `healthpersonnel`
--

LOCK TABLES `healthpersonnel` WRITE;
/*!40000 ALTER TABLE `healthpersonnel` DISABLE KEYS */;
INSERT INTO `healthpersonnel` VALUES (1,'Santos','Maria','Nurse','General','09180001111'),(2,'Garcia','Anna','Doctor','General Practice','09301234567'),(3,'Martinez','Joy','Nurse','Community Health','09311234567'),(4,'Cruz','Ramon','Dentist','Oral Health','09321234567'),(5,'Santos','Linda','Midwife','Maternal Health','09331234567'),(6,'Reyes','Miguel','Pharmacist','Pharmacy','09341234567'),(7,'Garcia','Anna','Doctor','General Practice','09301234567'),(8,'Martinez','Joy','Nurse','Community Health','09311234567'),(9,'Cruz','Ramon','Dentist','Oral Health','09321234567'),(10,'Santos','Linda','Midwife','Maternal Health','09331234567'),(11,'Reyes','Miguel','Pharmacist','Pharmacy','09341234567');
/*!40000 ALTER TABLE `healthpersonnel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `healthservices`
--

DROP TABLE IF EXISTS `healthservices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `healthservices` (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `service_type` varchar(50) NOT NULL,
  `description` text,
  `fee` decimal(10,2) DEFAULT '0.00',
  `remarks` text,
  PRIMARY KEY (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `healthservices`
--

LOCK TABLES `healthservices` WRITE;
/*!40000 ALTER TABLE `healthservices` DISABLE KEYS */;
INSERT INTO `healthservices` VALUES (1,'Check-up','General medical consultation',50.00,NULL),(2,'Medical Consultation','General health checkup and diagnosis',50.00,'Free for senior citizens and PWD'),(3,'Vaccination','Immunization services for all ages',0.00,'Government sponsored program'),(4,'Prenatal Checkup','Pregnancy monitoring and care',100.00,'Includes vitamins and supplements'),(5,'Dental Cleaning','Oral health maintenance and cleaning',150.00,'Per session'),(6,'Blood Pressure Monitoring','BP check and monitoring',0.00,'Free service'),(7,'Family Planning','Contraception counseling and services',0.00,'Government sponsored'),(8,'Deworming','Anti-parasitic treatment',0.00,'For children and adults'),(9,'Medical Consultation','General health checkup and diagnosis',50.00,'Free for senior citizens and PWD'),(10,'Vaccination','Immunization services for all ages',0.00,'Government sponsored program'),(11,'Prenatal Checkup','Pregnancy monitoring and care',100.00,'Includes vitamins and supplements'),(12,'Dental Cleaning','Oral health maintenance and cleaning',150.00,'Per session'),(13,'Blood Pressure Monitoring','BP check and monitoring',0.00,'Free service'),(14,'Family Planning','Contraception counseling and services',0.00,'Government sponsored'),(15,'Deworming','Anti-parasitic treatment',0.00,'For children and adults');
/*!40000 ALTER TABLE `healthservices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `residents`
--

DROP TABLE IF EXISTS `residents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `residents` (
  `resident_id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `age` int DEFAULT NULL,
  `sex` char(1) DEFAULT NULL,
  `contact_number` varchar(11) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `household_id` int DEFAULT NULL,
  `vulnerability_status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`resident_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `residents`
--

LOCK TABLES `residents` WRITE;
/*!40000 ALTER TABLE `residents` DISABLE KEYS */;
INSERT INTO `residents` VALUES (1,'Kim','Ver',14,'F','12341234123','Baguio',NULL,'None'),(2,'Juan','Dela Cruz',35,'M','09171234567','Purok 1, Barangay Centro',1001,'None'),(3,'Maria','Santos',28,'F','09181234567','Purok 2, Barangay Centro',1002,'Pregnant'),(4,'Pedro','Reyes',42,'M','09191234567','Purok 3, Barangay Centro',1003,'Senior Citizen'),(5,'Ana','Garcia',65,'F','09201234567','Purok 4, Barangay Centro',1004,'PWD'),(6,'Jose','Ramos',19,'M','09211234567','Purok 5, Barangay Centro',NULL,'None'),(7,'Rosa','Cruz',52,'F','09221234567','Purok 6, Barangay Centro',1005,'Diabetic'),(8,'Carlos','Lopez',8,'M','09231234567','Purok 1, Barangay Centro',1001,'Child'),(9,'Elena','Fernandez',45,'F','09241234567','Purok 7, Barangay Centro',1006,'Hypertensive'),(10,'Juan','Dela Cruz',35,'M','09171234567','Purok 2, Barangay Malabon',1001,'None'),(11,'Maria','Santos',28,'F','09181234567','Purok 2, Barangay Centro',1002,'Pregnant'),(12,'Pedro','Reyes',42,'M','09191234567','Purok 3, Barangay Centro',1003,'Senior Citizen'),(13,'Ana','Garcia',65,'F','09201234567','Purok 4, Barangay Centro',1004,'PWD'),(14,'Jose','Ramos',19,'M','09211234567','Purok 5, Barangay Centro',NULL,'None'),(15,'Rosa','Cruz',52,'F','09221234567','Purok 6, Barangay Centro',1005,'Diabetic'),(16,'Carlos','Lopez',8,'M','09231234567','Purok 1, Barangay Centro',1001,'Child'),(17,'Elena','Fernandez',45,'F','09241234567','Purok 7, Barangay Centro',1006,'Hypertensive'),(18,'Theodore','Bruh',19,'M','09165262401','Purok 6, Barangay Last Pinya',1005,'Diabetic'),(19,'Big','Ced',18,'G','09165265266','Las Pinas',NULL,'None'),(20,'King','Jesmer',11,'M','09165557777','Sigma Rizz',44,NULL),(21,'Hello','R',12,'M','09165557477','Sigma Rizz',NULL,'None');
/*!40000 ALTER TABLE `residents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `serviceinventory`
--

DROP TABLE IF EXISTS `serviceinventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `serviceinventory` (
  `inventory_id` int NOT NULL AUTO_INCREMENT,
  `item_id` int DEFAULT NULL,
  `service_id` int DEFAULT NULL,
  `transaction_id` int DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `expiration` date DEFAULT NULL,
  PRIMARY KEY (`inventory_id`),
  KEY `item_id` (`item_id`),
  KEY `service_id` (`service_id`),
  KEY `transaction_id` (`transaction_id`),
  CONSTRAINT `serviceinventory_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `clinicinventory` (`item_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `serviceinventory_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `healthservices` (`service_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `serviceinventory_ibfk_3` FOREIGN KEY (`transaction_id`) REFERENCES `servicetransactions` (`transaction_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `serviceinventory`
--

LOCK TABLES `serviceinventory` WRITE;
/*!40000 ALTER TABLE `serviceinventory` DISABLE KEYS */;
INSERT INTO `serviceinventory` VALUES (1,1,1,1,2,'2026-12-31');
/*!40000 ALTER TABLE `serviceinventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicetransactions`
--

DROP TABLE IF EXISTS `servicetransactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicetransactions` (
  `transaction_id` int NOT NULL AUTO_INCREMENT,
  `service_id` int DEFAULT NULL,
  `resident_id` int DEFAULT NULL,
  `personnel_id` int DEFAULT NULL,
  `date_provided` date DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`transaction_id`),
  KEY `service_id` (`service_id`),
  KEY `resident_id` (`resident_id`),
  KEY `personnel_id` (`personnel_id`),
  CONSTRAINT `servicetransactions_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `healthservices` (`service_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `servicetransactions_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `servicetransactions_ibfk_3` FOREIGN KEY (`personnel_id`) REFERENCES `healthpersonnel` (`personnel_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicetransactions`
--

LOCK TABLES `servicetransactions` WRITE;
/*!40000 ALTER TABLE `servicetransactions` DISABLE KEYS */;
INSERT INTO `servicetransactions` VALUES (1,1,NULL,1,'2025-11-09','Routine check-up'),(2,1,NULL,1,'2024-11-01','Patient complains of fever and headache'),(3,2,2,2,'2024-11-02','Flu vaccine administered'),(4,3,2,1,'2024-11-03','First trimester checkup - normal'),(5,5,3,2,'2024-11-04','BP: 140/90 - slightly elevated'),(6,4,4,3,'2024-11-05','Dental scaling completed'),(7,7,7,2,'2024-11-06','Deworming medication given'),(8,1,NULL,1,'2024-11-01','Patient complains of fever and headache'),(9,2,2,2,'2024-11-02','Flu vaccine administered'),(10,3,2,1,'2024-11-03','First trimester checkup - normal'),(11,5,3,2,'2024-11-04','BP: 140/90 - slightly elevated'),(12,4,4,3,'2024-11-05','Dental scaling completed'),(13,7,7,2,'2024-11-06','Deworming medication given');
/*!40000 ALTER TABLE `servicetransactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('Admin','Personnel','Staff') NOT NULL,
  `personnel_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  KEY `personnel_id` (`personnel_id`),
  KEY `idx_username` (`username`),
  KEY `idx_role` (`role`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`personnel_id`) REFERENCES `healthpersonnel` (`personnel_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

ALTER TABLE ServiceTransactions
ADD COLUMN visit_type VARCHAR(20),
ADD COLUMN diagnosis TEXT,
ADD COLUMN treatment TEXT,
ADD COLUMN medical_notes TEXT;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'theo','theo123','Admin',NULL),(2,'ara','araara123','Admin',NULL),(3,'nelle','wordle123','Admin',NULL),(4,'karl','karlkarl123','Admin',NULL),(5,'staff1','staff123','Staff',NULL),(6,'annagarcia','ana123123','Staff',NULL),(7,'cruzramon','cr1234','Personnel',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-11  8:40:16
