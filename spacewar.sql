-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 18 Bulan Mei 2025 pada 16.52
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `spacewar`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `high_scores`
--

CREATE TABLE `high_scores` (
  `id` int(11) NOT NULL,
  `player_name` varchar(50) NOT NULL,
  `score` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `high_scores`
--

INSERT INTO `high_scores` (`id`, `player_name`, `score`, `date`) VALUES
(1, 'ryan\\', 0, '2025-05-17 18:38:40'),
(2, 'wrwe', 480, '2025-05-17 18:39:19'),
(3, 'rrr', 230, '2025-05-17 18:46:44'),
(4, 'dd', 520, '2025-05-18 07:15:55'),
(5, 'awa', 30, '2025-05-18 07:16:54'),
(6, 'OO', 20, '2025-05-18 07:23:06'),
(7, 'EE', 20, '2025-05-18 07:23:55'),
(8, 'jj', 10, '2025-05-18 07:34:38'),
(9, ' ww', 50, '2025-05-18 07:36:23'),
(10, ' ww', 0, '2025-05-18 07:40:50'),
(11, 'wa', 10, '2025-05-18 07:43:53'),
(12, 'w', 10, '2025-05-18 07:43:54'),
(13, '2w', 10, '2025-05-18 07:45:28'),
(14, 'wq', 60, '2025-05-18 07:46:39'),
(15, 'ww', 120, '2025-05-18 07:57:11'),
(16, 'ww', 50, '2025-05-18 08:02:23'),
(17, 'd', 10, '2025-05-18 08:04:02'),
(18, 'rr', 60, '2025-05-18 08:14:03'),
(19, 'w', 20, '2025-05-18 08:21:19'),
(20, '3', 20, '2025-05-18 08:21:47'),
(21, 'q', 40, '2025-05-18 08:22:46'),
(22, ' w', 0, '2025-05-18 08:27:00'),
(23, 'w', 0, '2025-05-18 08:28:52'),
(24, 'w', 30, '2025-05-18 08:29:07'),
(25, 'ww', 60, '2025-05-18 08:32:17'),
(26, 'w', 30, '2025-05-18 08:32:30'),
(27, 'ww', 20, '2025-05-18 08:50:51'),
(28, 'w', 0, '2025-05-18 08:51:54'),
(29, 'w', 60, '2025-05-18 08:52:03'),
(30, 's', 90, '2025-05-18 08:54:12'),
(31, 'w', 50, '2025-05-18 08:54:23'),
(32, 'ww', 0, '2025-05-18 08:54:38'),
(33, 'w', 10, '2025-05-18 08:54:52'),
(34, 'd', 50, '2025-05-18 08:56:14'),
(35, 's', 10, '2025-05-18 08:59:26'),
(36, 'w', 50, '2025-05-18 08:59:54'),
(37, 'l', 70, '2025-05-18 09:09:08'),
(38, 'ww', 90, '2025-05-18 09:28:11'),
(39, 'w', 90, '2025-05-18 09:28:53'),
(40, 'w', 10, '2025-05-18 09:29:01'),
(41, 'w', 435, '2025-05-18 10:17:05'),
(42, '2', 60, '2025-05-18 10:28:29'),
(43, ' anita', 120, '2025-05-18 12:51:17');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `high_scores`
--
ALTER TABLE `high_scores`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `high_scores`
--
ALTER TABLE `high_scores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
