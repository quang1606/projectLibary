package com.example.projectlibary;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.common.NewsStatus;
import com.example.projectlibary.common.UserRole;
import com.example.projectlibary.model.*;
import com.example.projectlibary.repository.*;
import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class ProjectLibaryApplicationTests {
 @Autowired
 private UserRepository userRepository;
 @Autowired
 private BCryptPasswordEncoder bCryptPasswordEncoder;
 @Autowired
 private CategoryRepository categoryRepository;
 @Autowired
 private BookRepository bookRepository;
 @Autowired
 private BookCopyRepository bookCopyRepository;
 @Autowired
 private AuthorRepository authorRepository;
 @Autowired
 private BookReviewRepository bookReviewRepository;
 @Autowired
 private NewsRepository newsRepository;

    @Test
    void generate_fake_users() {
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            String fullName = faker.name().fullName();
            String username;
            do {
                username = faker.name().username();
            } while (userRepository.existsByUsername(username));

            String email;
            do {
                email = faker.internet().emailAddress();
            } while (userRepository.existsByEmail(email));

            String phoneNumber;
            do {
                phoneNumber = faker.numerify("09########");
            } while (userRepository.existsByPhoneNumber(phoneNumber));

            String studentId = null;
            if (i >= 20 && i < 80) { // Chỉ sinh viên có studentId
                do {
                    studentId = "SV" + faker.numerify("######");
                } while (userRepository.existsByStudentId(studentId));
            }

            User user = User.builder()
                    .username(username)
                    .password("123") // "123456" đã hash
                    .email(email)
                    .fullName(fullName)
                    .studentId(studentId)
                    .phoneNumber(phoneNumber)
                    .avatar("https://placehold.co/600x400?text="+fullName.substring(0,1).toLowerCase())
                    .role(i < 5 ? UserRole.ADMIN : (i < 20 ? UserRole.LIBRARIAN : UserRole.STUDENT))
                    .isActive(true)
                    .build();

            // Các trường từ AbstractEntity được thiết lập thông qua @PrePersist
            userRepository.save(user);
        }
    }
    @Test
    void update_user_pasword(){
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String password = user.getPassword();
            String newPassword = bCryptPasswordEncoder.encode(password);
            user.setPassword(newPassword);
            userRepository.save(user);
        }
    }
    @Test
    void generate_fake_categories() {
        Faker faker = new Faker();

        // Danh sách các thể loại sách phổ biến
        String[] categoryNames = {
                "Tiểu thuyết", "Khoa học viễn tưởng", "Tự truyện", "Lịch sử",
                "Trinh thám", "Tâm lý học", "Kinh doanh", "Kỹ năng sống",
                "Triết học", "Văn học cổ điển", "Văn học hiện đại", "Khoa học",
                "Toán học", "Công nghệ thông tin", "Ngoại ngữ", "Kinh tế học",
                "Nghệ thuật", "Âm nhạc", "Thể thao", "Du lịch",
                "Ẩm thực", "Y học", "Kiến trúc", "Tôn giáo"
        };

        for (String categoryName : categoryNames) {
            // Kiểm tra xem thể loại đã tồn tại chưa
            if (!categoryRepository.existsByName(categoryName)) {
                // Tạo mô tả phong phú cho thể loại
                String description = faker.lorem().paragraph(3);

                Category category = Category.builder()
                        .name(categoryName)
                        .description(description)
                        .books(new HashSet<>()) // Khởi tạo set rỗng để tránh NullPointerException

                        .build();


                categoryRepository.save(category);
            }
        }
    }
    @Test
    void generate_fake_authors() {
        Faker faker = new Faker(new Locale("en-US")); // Sử dụng locale tiếng Anh để có tên tác giả thực tế hơn

        // Danh sách một số tác giả nổi tiếng Việt Nam
        String[] vietnameseAuthors = {
                "Nguyễn Du", "Nam Cao", "Tô Hoài", "Ngô Tất Tố", "Nguyễn Nhật Ánh",
                "Dương Thu Hương", "Bảo Ninh", "Nguyễn Huy Thiệp", "Vũ Trọng Phụng",
                "Thạch Lam", "Xuân Diệu", "Huy Cận", "Tố Hữu", "Hàn Mặc Tử", "Bà Huyện Thanh Quan",
                "Nguyễn Tuân", "Hồ Xuân Hương", "Trần Đăng Khoa", "Nguyễn Quang Sáng", "Nguyễn Thị Hoàng"
        };

        // Danh sách một số tác giả nổi tiếng quốc tế
        String[] internationalAuthors = {
                "J.K. Rowling", "George R.R. Martin", "Stephen King", "Agatha Christie", "Jane Austen",
                "Ernest Hemingway", "Leo Tolstoy", "Fyodor Dostoevsky", "Gabriel García Márquez", "Mark Twain",
                "Charles Dickens", "William Shakespeare", "Haruki Murakami", "Franz Kafka", "Victor Hugo",
                "Toni Morrison", "Virginia Woolf", "F. Scott Fitzgerald", "George Orwell", "Emily Dickinson",
                "Dan Brown", "Paulo Coelho", "Tolkien", "Neil Gaiman", "Khaled Hosseini"
        };

        // Kết hợp cả hai danh sách
        List<String> allAuthors = new ArrayList<>();
        allAuthors.addAll(Arrays.asList(vietnameseAuthors));
        allAuthors.addAll(Arrays.asList(internationalAuthors));

        // Thêm một số tác giả ngẫu nhiên
        for (int i = 0; i < 20; i++) {
            String randomName = faker.name().fullName();
            allAuthors.add(randomName);
        }

        // Tạo dữ liệu tác giả
        for (String authorName : allAuthors) {
            // Kiểm tra xem tác giả đã tồn tại trong database chưa
            if (!authorRepository.existsByName(authorName)) {
                // Tạo tiểu sử tác giả ngẫu nhiên
                String bio = faker.lorem().paragraphs(faker.number().numberBetween(2, 5))
                        .stream().collect(Collectors.joining("\n\n"));

                Author author = Author.builder()
                        .name(authorName)
                        .bio(bio)
                        .build();

                authorRepository.save(author);
            }
        }
    }
    @Test
    @Transactional
    @Rollback(false) // Hoặc @Commit
    void generate_fake_books() {
        Faker faker = new Faker(new Locale("vi"));

        // Lấy tất cả categories và authors từ database
        List<Category> categories = categoryRepository.findAll();
        List<Author> authors = authorRepository.findAll();

        // Lấy danh sách admin/librarian (người có quyền thêm sách)
        List<User> librarians = userRepository.findByRoleIn((Arrays.asList( UserRole.LIBRARIAN)));

        // Kiểm tra xem có đủ dữ liệu để tạo sách không
        if (categories.isEmpty() || authors.isEmpty() || librarians.isEmpty()) {
            System.out.println("Cần có dữ liệu về thể loại, tác giả và thủ thư trước khi tạo sách");
            return;
        }

        for (int i = 0; i < 200; i++) {
            // Tạo ISBN duy nhất
            String isbn;
            do {
                isbn = "ISBN" + faker.number().digits(10);
            } while (bookRepository.existsByIsbn(isbn));

            // Chọn ngẫu nhiên category, authors và người tạo
            Category category = categories.get(faker.number().numberBetween(0, categories.size()));
            User creator = librarians.get(faker.number().numberBetween(0, librarians.size()));

            // Chọn ngẫu nhiên 1-3 tác giả cho mỗi sách
            Set<Author> bookAuthors = new HashSet<>();
            int authorCount = faker.number().numberBetween(1, 4);
            for (int j = 0; j < authorCount; j++) {
                Author author = authors.get(faker.number().numberBetween(0, authors.size()));

                bookAuthors.add(author);
            }

            // Tạo sách mới
            Book book = Book.builder()
                    .title(faker.book().title())
                    .description(faker.lorem().paragraphs(3).stream().collect(Collectors.joining("\n")))
                    .isbn(isbn)
                    .category(category)
                    .publisher(faker.company().name())
                    .publicationYear(faker.number().numberBetween(1950, 2023))
                    .ebookUrl(faker.random().nextBoolean() ? "https://example.com/ebooks/" + faker.internet().slug() + ".pdf" : null)
                    .replacementCost(new BigDecimal(faker.number().randomDouble(2, 100000, 500000)))
                    .createdBy(creator)
                    .updatedBy(creator)
                    .authors(bookAuthors)
                    .build();

            bookRepository.save(book);

        }
    }
    @Test
    public void generate_fake_book_copies() {
        Faker faker = new Faker(new Locale("vi"));

        // Lấy danh sách sách từ cơ sở dữ liệu
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            System.out.println("Không có sách nào trong cơ sở dữ liệu. Vui lòng tạo sách trước.");
            return;
        }

        // Lấy danh sách người dùng là thủ thư hoặc admin
        List<User> librarians = userRepository.findByRoleIn(Arrays.asList(UserRole.ADMIN, UserRole.LIBRARIAN));
        if (librarians.isEmpty()) {
            System.out.println("Không có thủ thư hoặc admin trong cơ sở dữ liệu.");
            return;
        }

        // Tạo bản sao cho mỗi cuốn sách
        for (Book book : books) {
            // Số bản sao ngẫu nhiên cho mỗi cuốn sách (từ 1 đến 5)
            int copyCount = faker.number().numberBetween(1, 6);

            for (int i = 1; i <= copyCount; i++) {
                // Tạo copyNumber duy nhất dựa trên ISBN của sách
                String copyNumber = String.format("%s-%03d", book.getIsbn(), i);

                // Kiểm tra xem bản sao đã tồn tại chưa
                if (bookCopyRepository.existsByCopyNumber(copyNumber)) {
                    continue; // Bỏ qua nếu đã tồn tại
                }

                // Tạo mã QR duy nhất (trong thực tế, bạn có thể sử dụng thư viện tạo mã QR)
                String qrCode = UUID.randomUUID().toString();

                // Chọn ngẫu nhiên trạng thái cho bản sao với tỷ lệ phân bổ hợp lý
                BookCopyStatus status = getRandomStatus(faker);

                // Chọn ngẫu nhiên vị trí kệ
                String location = generateShelfLocation(faker);

                // Chọn ngẫu nhiên ngày thêm vào thư viện (trong 365 ngày gần đây)
                LocalDate addedDate = LocalDate.now().minusDays(faker.number().numberBetween(0, 365));

                // Chọn ngẫu nhiên người tạo/cập nhật
                User creator = librarians.get(faker.number().numberBetween(0, librarians.size()));

                BookCopy bookCopy = BookCopy.builder()
                        .book(book)
                        .copyNumber(copyNumber)
                        .qrCode(qrCode)
                        .status(status)
                        .location(location)
                        .addedDate(addedDate)
                        .createdBy(creator)
                        .updatedBy(creator)
                        .bookLoans(new HashSet<>())
                        .build();

                try {
                    bookCopyRepository.save(bookCopy);
                } catch (Exception e) {
                    System.out.println("Lỗi khi lưu bản sao sách: " + copyNumber);
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method để lấy trạng thái ngẫu nhiên với tỷ lệ phân bổ hợp lý
    private BookCopyStatus getRandomStatus(Faker faker) {
        // Tỷ lệ phân bổ trạng thái:
        // 60% AVAILABLE, 15% BORROWED, 10% PENDING_BORROW, 5% PENDING_RETURN, 4% DAMAGED, 3% REPAIRING, 3% LOST
        int randomNum = faker.number().numberBetween(1, 101);

        if (randomNum <= 60) {
            return BookCopyStatus.AVAILABLE;
        } else if (randomNum <= 75) {
            return BookCopyStatus.BORROWED;
        } else if (randomNum <= 85) {
            return BookCopyStatus.PENDING_BORROW;
        } else if (randomNum <= 90) {
            return BookCopyStatus.PENDING_RETURN;
        } else if (randomNum <= 94) {
            return BookCopyStatus.DAMAGED;
        } else if (randomNum <= 97) {
            return BookCopyStatus.REPAIRING;
        } else {
            return BookCopyStatus.LOST;
        }
    }
    // Helper method để tạo vị trí kệ ngẫu nhiên
    private String generateShelfLocation(Faker faker) {
        // Format: [Khu vực]-[Kệ]-[Hàng]
        String area = faker.options().option("A", "B", "C", "D");
        int shelf = faker.number().numberBetween(1, 21);
        int row = faker.number().numberBetween(1, 6);

        return String.format("%s-%02d-%d", area, shelf, row);
    }
    @Test
    public void generate_fake_book_reviews() {
        Faker faker = new Faker(new Locale("vi"));

        // Lấy danh sách sách từ cơ sở dữ liệu
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            System.out.println("Không có sách nào trong cơ sở dữ liệu. Vui lòng tạo sách trước.");
            return;
        }

        // Lấy danh sách người dùng từ cơ sở dữ liệu (chủ yếu là PATRON - người mượn sách)
        List<User> patrons = userRepository.findByRoleIn((Arrays.asList( UserRole.STUDENT)));
        if (patrons.isEmpty()) {
            System.out.println("Không có người dùng (PATRON) trong cơ sở dữ liệu. Vui lòng tạo người dùng trước.");
            return;
        }

        // Số lượng đánh giá muốn tạo
        int totalReviews = 200;
        int createdReviews = 0;
        int maxAttempts = totalReviews * 2; // Đề phòng trường hợp trùng lặp
        int attempts = 0;

        while (createdReviews < totalReviews && attempts < maxAttempts) {
            attempts++;

            // Chọn ngẫu nhiên một cuốn sách
            Book book = books.get(faker.number().numberBetween(0, books.size()));

            // Chọn ngẫu nhiên một người dùng
            User user = patrons.get(faker.number().numberBetween(0, patrons.size()));

            // Kiểm tra xem người dùng này đã đánh giá cuốn sách này chưa (unique constraint)
            if (bookReviewRepository.existsByUserAndBook(user, book)) {
                continue; // Người dùng đã đánh giá sách này, tiếp tục vòng lặp
            }

            // Tạo điểm đánh giá từ 1-5
            int rating = faker.number().numberBetween(1, 6);

            // Tạo bình luận dựa trên điểm đánh giá
            String comment = generateCommentBasedOnRating(faker, rating);

            // Tạo ngày đánh giá - trong khoảng 1-180 ngày gần đây
            LocalDateTime reviewDate = LocalDateTime.now().minusDays(faker.number().numberBetween(1, 181));

            BookReview bookReview = BookReview.builder()
                    .user(user)
                    .book(book)
                    .rating(rating)
                    .comment(comment)
                    .reviewDate(reviewDate)
                    .build();

            try {
                bookReviewRepository.save(bookReview);
                createdReviews++;

                // Cập nhật điểm đánh giá trung bình của sách (nếu có field này trong Book)
                // updateBookAverageRating(book.getId());

            } catch (Exception e) {
                System.out.println("Lỗi khi lưu đánh giá sách: Người dùng " + user.getId() + ", Sách " + book.getId());
                e.printStackTrace();
            }
        }

        System.out.println("Đã tạo thành công " + createdReviews + " đánh giá sách.");
    }

    // Helper method để tạo bình luận phù hợp với điểm đánh giá
    private String generateCommentBasedOnRating(Faker faker, int rating) {
        String[] positiveAdjectives = {
                "tuyệt vời", "xuất sắc", "ấn tượng", "đáng kinh ngạc", "đáng đọc",
                "hay", "thú vị", "hấp dẫn", "sâu sắc", "đầy cảm hứng"
        };

        String[] negativeAdjectives = {
                "kém", "nhàm chán", "đáng thất vọng", "yếu", "không đáng đọc",
                "tẻ nhạt", "khó hiểu", "nông cạn", "thiếu sức hút", "kém hấp dẫn"
        };

        String[] neutralAdjectives = {
                "bình thường", "trung bình", "ổn", "tạm được", "không đặc biệt",
                "có thể đọc", "không đáng nhớ", "có điểm tốt và điểm xấu", "cơ bản", "đủ xem"
        };

        StringBuilder comment = new StringBuilder();

        // Thêm phần mở đầu tùy theo đánh giá
        if (rating >= 4) {
            comment.append(faker.options().option(
                    "Tôi thực sự thích cuốn sách này. ",
                    "Một cuốn sách tuyệt vời! ",
                    "Đây là một trong những cuốn sách hay nhất tôi từng đọc. ",
                    "Tôi đánh giá rất cao cuốn sách này. ",
                    "Thật sự ấn tượng với cuốn sách này. "
            ));
        } else if (rating == 3) {
            comment.append(faker.options().option(
                    "Cuốn sách này khá ổn. ",
                    "Không quá xuất sắc nhưng cũng không tệ. ",
                    "Một cuốn sách đọc được. ",
                    "Cũng có những điểm hay. ",
                    "Sách có cả điểm mạnh và điểm yếu. "
            ));
        } else {
            comment.append(faker.options().option(
                    "Tôi không thực sự thích cuốn sách này. ",
                    "Cuốn sách này khiến tôi thất vọng. ",
                    "Có nhiều vấn đề với cuốn sách này. ",
                    "Không đáng với thời gian đọc. ",
                    "Tôi không thể đọc hết cuốn sách này. "
            ));
        }

        // Thêm nhận xét về nội dung
        if (rating >= 4) {
            comment.append(faker.options().option(
                    "Nội dung " + faker.options().option(positiveAdjectives) + " và " + faker.options().option(positiveAdjectives) + ". ",
                    "Tác giả đã xây dựng cốt truyện một cách " + faker.options().option(positiveAdjectives) + ". ",
                    "Các nhân vật được phát triển " + faker.options().option(positiveAdjectives) + ". ",
                    "Lối viết " + faker.options().option(positiveAdjectives) + " và cuốn hút người đọc. ",
                    "Chủ đề của sách rất " + faker.options().option(positiveAdjectives) + " và đáng suy ngẫm. "
            ));
        } else if (rating == 3) {
            comment.append(faker.options().option(
                    "Nội dung " + faker.options().option(neutralAdjectives) + ", nhưng " + faker.options().option(neutralAdjectives) + ". ",
                    "Cốt truyện " + faker.options().option(neutralAdjectives) + " nhưng còn một số điểm chưa hợp lý. ",
                    "Nhân vật " + faker.options().option(neutralAdjectives) + " nhưng chưa thực sự sâu sắc. ",
                    "Lối viết " + faker.options().option(neutralAdjectives) + " nhưng đôi khi hơi dài dòng. ",
                    "Chủ đề thì " + faker.options().option(neutralAdjectives) + ". "
            ));
        } else {
            comment.append(faker.options().option(
                    "Nội dung " + faker.options().option(negativeAdjectives) + " và " + faker.options().option(negativeAdjectives) + ". ",
                    "Cốt truyện " + faker.options().option(negativeAdjectives) + " và thiếu logic. ",
                    "Nhân vật " + faker.options().option(negativeAdjectives) + " và không đáng nhớ. ",
                    "Lối viết " + faker.options().option(negativeAdjectives) + " và gây khó chịu. ",
                    "Chủ đề quá " + faker.options().option(negativeAdjectives) + ". "
            ));
        }

        // Thêm kết luận
        if (rating >= 4) {
            comment.append(faker.options().option(
                    "Tôi chắc chắn sẽ đọc thêm sách của tác giả này.",
                    "Tôi hoàn toàn đề xuất cuốn sách này cho những ai yêu thích thể loại này.",
                    "Đây là một cuốn sách đáng được thêm vào bộ sưu tập của bạn.",
                    "Tôi rất mong đợi được đọc các tác phẩm khác của tác giả.",
                    "Sách xứng đáng được nhiều người biết đến hơn nữa."
            ));
        } else if (rating == 3) {
            comment.append(faker.options().option(
                    "Sách có thể phù hợp với một số người, nhưng không phải tất cả.",
                    "Nếu bạn không có gì khác để đọc, sách này có thể là một lựa chọn.",
                    "Không tệ, nhưng cũng không đặc biệt xuất sắc.",
                    "Có thể thử đọc nếu bạn thích thể loại này.",
                    "Sách này có những ưu điểm nhất định, nhưng cũng còn nhiều điểm cần cải thiện."
            ));
        } else {
            comment.append(faker.options().option(
                    "Tôi không khuyến khích đọc cuốn sách này.",
                    "Có nhiều lựa chọn tốt hơn trong thể loại này.",
                    "Tôi thấy tiếc thời gian đã bỏ ra để đọc nó.",
                    "Thật khó để tìm thấy điểm tích cực trong cuốn sách này.",
                    "Tôi sẽ không đọc thêm sách của tác giả này."
            ));
        }

        return comment.toString();
    }
    @Test
    public void generate_fake_news() {
        Faker faker = new Faker(new Locale("vi"));

        // Lấy danh sách người dùng có vai trò ADMIN và LIBRARIAN
        List<User> staffUsers = userRepository.findByRoleIn(Arrays.asList(UserRole.ADMIN, UserRole.LIBRARIAN));
        if (staffUsers.isEmpty()) {
            System.out.println("Không có người dùng ADMIN hoặc LIBRARIAN trong cơ sở dữ liệu. Vui lòng tạo người dùng trước.");
            return;
        }

        // Số lượng tin tức muốn tạo
        int totalNews = 50;
        int createdNews = 0;

        // Tạo các tiêu đề tin tức phổ biến cho thư viện
        String[] newsPrefixes = {
                "Thông báo:", "Sự kiện:", "Cập nhật:", "Tin mới:", "Thông tin:",
                "Chương trình:", "Triển lãm:", "Hội thảo:", "Khóa học:", "Giới thiệu:"
        };

        String[] newsTopics = {
                "sách mới", "tác giả nổi tiếng", "hoạt động đọc sách", "thay đổi giờ mở cửa",
                "cuộc thi viết", "triển lãm sách", "hội thảo văn học", "quy định mới",
                "ưu đãi thành viên", "sự kiện giao lưu", "giới thiệu sách hay",
                "đầu sách bán chạy", "góc đọc sách mới", "tuyển tình nguyện viên",
                "hoạt động hè", "ngày hội đọc sách", "lịch sự kiện tháng", "tác giả ghé thăm"
        };

        for (int i = 0; i < totalNews; i++) {
            // Chọn ngẫu nhiên người tạo và người cập nhật
            User creator = staffUsers.get(faker.number().numberBetween(0, staffUsers.size()));
            User updater = faker.bool().bool() ? creator : staffUsers.get(faker.number().numberBetween(0, staffUsers.size()));

            // Tạo tiêu đề ngẫu nhiên
            String prefix = newsPrefixes[faker.number().numberBetween(0, newsPrefixes.length)];
            String topic = newsTopics[faker.number().numberBetween(0, newsTopics.length)];
            String title = prefix + " " + topic + " " + faker.book().title();

            // Giới hạn độ dài tiêu đề
            if (title.length() > 255) {
                title = title.substring(0, 252) + "...";
            }

            // Tạo nội dung tin tức
            String content = generateNewsContent(faker);

            // Xác định trạng thái tin tức
            NewsStatus status;
            int statusRandom = faker.number().numberBetween(1, 101);
            if (statusRandom <= 60) {
                status = NewsStatus.PUBLISHED; // 60% published
            } else if (statusRandom <= 85) {
                status = NewsStatus.DRAFT; // 25% draft
            } else {
                status = NewsStatus.ARCHIVED; // 15% archived
            }

            News news = News.builder()
                    .title(title)
                    .content(content)
                    .status(status)
                    .createdBy(creator)
                    .updatedBy(updater)
                    .build();

            try {
                newsRepository.save(news);
                createdNews++;
            } catch (Exception e) {
                System.out.println("Lỗi khi lưu tin tức: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Đã tạo thành công " + createdNews + " tin tức.");
    }

    // Helper method để tạo nội dung tin tức
    private String generateNewsContent(Faker faker) {
        StringBuilder content = new StringBuilder();

        // Thêm đoạn mở đầu
        content.append("<p>").append(faker.lorem().paragraph(3)).append("</p>\n\n");

        // Thêm tiêu đề nhỏ
        content.append("<h3>").append(faker.book().title()).append("</h3>\n");

        // Thêm nội dung chính gồm 2-4 đoạn
        int paragraphs = faker.number().numberBetween(2, 5);
        for (int i = 0; i < paragraphs; i++) {
            content.append("<p>").append(faker.lorem().paragraph(faker.number().numberBetween(3, 8))).append("</p>\n");
        }

        // Thêm danh sách ngẫu nhiên
        boolean addList = faker.bool().bool();
        if (addList) {
            content.append("<ul>\n");
            int listItems = faker.number().numberBetween(3, 7);
            for (int i = 0; i < listItems; i++) {
                content.append("<li>").append(faker.book().title()).append("</li>\n");
            }
            content.append("</ul>\n");
        }

        // Thêm thông tin liên hệ
        content.append("<p><strong>Thông tin liên hệ:</strong> ");
        content.append("Email: library@example.com | Điện thoại: ").append(faker.phoneNumber().phoneNumber());
        content.append("</p>\n");

        // Thêm chữ ký
        content.append("<p>Trân trọng,<br>Ban quản lý thư viện</p>");

        return content.toString();
    }
    @Test
    void contextLoads() {
    }

}
