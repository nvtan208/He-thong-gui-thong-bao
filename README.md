# 🚀 Ứng dụng gửi thông báo các thông tin hàng ngày như thời tiết, tin tức.

**Notification System** là ứng dụng **Client/Server** gửi thông báo hàng ngày như **thời tiết 🌤️**, **tin tức 📰** đến nhiều client, với giao diện trực quan và dễ tương tác.  

---

## ✨ Tính năng

### Server 🖥️
- 📡 Gửi thông báo **thời tiết** và **tin tức** đến tất cả các client.
- ✏️ Tạo thông báo thủ công từ giao diện quản lý.
- 📋 Hiển thị **nhật ký hoạt động** và **danh sách client**.
- 🚫 Chặn client và gửi lý do chặn.
- 🌐 Lấy dữ liệu từ API (Weather & News) và gửi tự động theo định kỳ.
- 🔄 Hỗ trợ nhiều client kết nối đồng thời.

### Client 💻
- 🗂️ Hiển thị thông báo dạng **card trực quan**.
- 🔍 Xem chi tiết nội dung thông báo.
- 🔗 Hiển thị trạng thái kết nối với server.
- 📴 Ngắt kết nối & 🔄 Kết nối lại.
- ⚠️ Thông báo khi client bị chặn và lý do chặn.

---

## 📂 Cấu trúc dự án

```
notification/
├── NotificationServer.java      # Server chính
├── NotificationClient.java      # Client nhận thông báo
├── APIHelper.java               # Lấy dữ liệu từ API
├── IconManager.java             # Quản lý icon UI
├── /icons/                      # Thư mục chứa icon
└── README.md                    # File hướng dẫn
```

---

## 🛠️ Yêu cầu hệ thống
- Java JDK 11 trở lên
- Kết nối mạng (API & Client-Server)
- IDE (IntelliJ, Eclipse, NetBeans) hoặc chạy trực tiếp từ terminal

---

## 🚀 Hướng dẫn sử dụng

### Chạy server
1. Biên dịch `NotificationServer.java`.
2. Chạy server:
```bash
java notification.NotificationServer
```
3. Giao diện server:
   - Tab **Nhật ký**: nhật ký hoạt động.
   - Tab **Thông báo**: danh sách thông báo.
   - Tab **Client**: quản lý client & chặn client.
   - Control Panel: tạo thông báo thủ công hoặc gửi lại thông báo đã chọn.

### Chạy client
1. Biên dịch `NotificationClient.java`.
2. Chạy client:
```bash
java notification.NotificationClient
```
3. Giao diện client:
   - Hiển thị thông báo dạng **card**.
   - Click vào card để xem chi tiết.
   - Hiển thị trạng thái kết nối.
   - Nút **Ngắt kết nối** và **Kết nối lại**.

---

## 🔑 Cấu hình API

- Mở `APIHelper.java`.
- Thêm API key của bạn:
```java
private static final String WEATHER_API_KEY = "YOUR_WEATHER_API_KEY";
private static final String NEWS_API_KEY = "YOUR_NEWS_API_KEY";
```
- Server sẽ tự động lấy dữ liệu và gửi thông báo theo định kỳ.

---

## 💡 Tính năng mở rộng
- Thêm nhiều loại thông báo khác: thể thao, giao thông, sự kiện...
- Lưu lịch sử thông báo cho client mới kết nối.
- Tích hợp bảo mật: xác thực, mã hóa dữ liệu.
- Gửi thông báo theo nhóm client.

---

## 🎨 Giao diện

### Server 🖥️
- Tab **Nhật ký**: hiển thị sự kiện server & client.
- Tab **Thông báo**: danh sách thông báo.
- Tab **Client**: danh sách client & chức năng chặn.
- Control Panel: tạo thông báo thủ công hoặc gửi thông báo đã chọn.

### Client 💻
- Hiển thị thông báo dạng **card**.
- Click vào card để xem chi tiết.
- Thông báo trạng thái kết nối.
- Nút **Ngắt kết nối** & **Kết nối lại**.

---

## 📜 License
Dự án **mã nguồn mở**, bạn có thể sử dụng, chỉnh sửa và phân phối tự do.

---
