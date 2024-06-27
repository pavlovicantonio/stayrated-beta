import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UserApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Connection connection;
    private int currentUserId;
    private String currentUserName;
    private JPanel repliesPanel;
    private int currentCommentId;

    public UserApp() {
        setTitle("StayRated");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        initializeDBConnection();

        JPanel loginPanel = createLoginPanel();
        JPanel registerPanel = createRegisterPanel();
        JPanel homePanel = createHomePanel();
        JPanel repliesPanel = createRepliesPanel();

        cardPanel.add(loginPanel, "Login");
        cardPanel.add(registerPanel, "Register");
        cardPanel.add(homePanel, "Home");
        cardPanel.add(repliesPanel, "Replies");

        add(cardPanel);

        cardLayout.show(cardPanel, "Login");

        setVisible(true);
    }

    private void initializeDBConnection() {
        String jdbcURL = "jdbc:mysql://localhost:3306/stayrated";
        String username = "root";
        String password = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Successfully connected to the database.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "MySQL Driver not found!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection to database failed!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void resetLoginFields(JTextField emailField, JPasswordField passwordField) {
        emailField.setText("");
        passwordField.setText("");
    }

    private void resetRegisterFields(JTextField firstNameField, JTextField lastNameField, JTextField emailField,
            JPasswordField passwordField) {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (verifyLogin(email, password)) {
                    JOptionPane.showMessageDialog(panel, "Login successful!");
                    cardLayout.show(cardPanel, "Home");
                    loadComments(commentsPanel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid email or password!");
                }
            }
        });
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        JLabel registerLabel = new JLabel("Don't have an account? Register now!");
        registerLabel.setForeground(Color.BLUE);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardLayout.show(cardPanel, "Register");
                resetLoginFields(emailField, passwordField);
            }
        });
        panel.add(registerLabel, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        JButton registerButton = new JButton("Register");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(registerButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (!email.contains("@")) {
                    JOptionPane.showMessageDialog(panel, "Email must contain @ symbol!");
                    return;
                }

                if (!isValidPassword(password)) {
                    JOptionPane.showMessageDialog(panel,
                            "Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, and a number!");
                    return;
                }

                if (registerUser(firstName, lastName, email, password)) {
                    JOptionPane.showMessageDialog(panel, "Registration successful!");
                    cardLayout.show(cardPanel, "Home");
                    loadComments(commentsPanel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Registration failed! Email may already be in use.");
                }
            }
        });

        JLabel loginLabel = new JLabel("You already have an account? Login now!");
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(loginLabel, gbc);

        loginLabel.setForeground(Color.BLUE);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardLayout.show(cardPanel, "Login");
                resetRegisterFields(firstNameField, lastNameField, emailField, passwordField);
            }
        });

        return panel;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasNumber = password.matches(".*\\d.*");
        return hasUppercase && hasLowercase && hasNumber;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentUserId = 0;
                currentUserName = null;
                cardLayout.show(cardPanel, "Login");
            }
        });

        JButton refreshButton = new JButton("Refresh Page");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadComments(commentsPanel);
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshButton);
        topPanel.add(logoutButton);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel userNameLabel = new JLabel();
        inputPanel.add(userNameLabel, BorderLayout.WEST);
        JTextField commentField = new JTextField();
        JButton postButton = new JButton("Post Comment");
        postButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String comment = commentField.getText();
                if (!comment.isEmpty()) {
                    postComment(comment);
                    commentField.setText("");
                    loadComments(commentsPanel);
                }
            }
        });
        inputPanel.add(commentField, BorderLayout.CENTER);
        inputPanel.add(postButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(commentsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                userNameLabel.setText(currentUserName);
                loadComments(commentsPanel);
            }
        });

        return panel;
    }

    private JPanel commentsPanel;

    private void loadComments(JPanel panel) {
        panel.removeAll(); // Clear previous comments
        try {
            String sql = "SELECT comments.id, users.first_name, users.last_name, comments.comment, comments.timestamp, comments.user_id, "
                    + "(SELECT COUNT(*) FROM comment_likes WHERE comment_likes.comment_id = comments.id) AS likes, "
                    + "(SELECT COUNT(*) FROM comment_likes WHERE comment_likes.comment_id = comments.id AND comment_likes.user_id = ?) AS liked_by_user, "
                    + "(SELECT COUNT(*) FROM replies WHERE replies.comment_id = comments.id) AS reply_count "
                    + "FROM comments "
                    + "JOIN users ON comments.user_id = users.id";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int commentId = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String comment = resultSet.getString("comment");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                int userId = resultSet.getInt("user_id");
                int likes = resultSet.getInt("likes");
                boolean likedByUser = resultSet.getInt("liked_by_user") > 0;
                int replyCount = resultSet.getInt("reply_count");

                JPanel commentPanel = new JPanel();
                commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
                commentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel nameLabel = new JLabel(firstName + " " + lastName + " - " + timestamp.toString());
                commentPanel.add(nameLabel);

                JTextArea commentArea = new JTextArea(comment);
                commentArea.setWrapStyleWord(true);
                commentArea.setLineWrap(true);
                commentArea.setEditable(false);
                commentArea.setBackground(Color.WHITE); // Set background color to white

                // Set preferred height based on content
                int lineCount = commentArea.getLineCount();
                int preferredHeight = Math.min(100 + lineCount * 15, 200); // Adjust the height limits as needed
                commentArea.setPreferredSize(new Dimension(commentArea.getPreferredSize().width, preferredHeight));

                commentPanel.add(commentArea);

                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                JButton likeButton = new JButton(likedByUser ? "Unlike" : "Like");
                likeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (likedByUser) {
                            unlikeComment(commentId);
                        } else {
                            likeComment(commentId);
                        }
                        loadComments(panel);
                    }
                });

                JLabel likesLabel = new JLabel(likes + " likes");
                actionsPanel.add(likesLabel);
                actionsPanel.add(likeButton);

                if (userId == currentUserId) {
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int confirm = JOptionPane.showConfirmDialog(null,
                                    "Are you sure you want to delete this comment?", "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                deleteComment(commentId);
                                loadComments(panel);
                            }
                        }
                    });
                    actionsPanel.add(deleteButton);
                }

                // Add reply count and view replies button
                JLabel replyCountLabel = new JLabel(replyCount + " replies");
                JButton viewRepliesButton = new JButton("View Replies");
                viewRepliesButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showRepliesPage(commentId);
                    }
                });
                actionsPanel.add(replyCountLabel);
                actionsPanel.add(viewRepliesButton);

                commentPanel.add(actionsPanel);

                panel.add(commentPanel);
                panel.add(Box.createVerticalStrut(10)); // Add space between comments
            }

            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createRepliesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "Home");
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        panel.add(topPanel, BorderLayout.NORTH);

        repliesPanel = new JPanel();
        repliesPanel.setLayout(new BoxLayout(repliesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(repliesPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add reply input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField replyField = new JTextField();
        JButton postReplyButton = new JButton("Post Reply");
        postReplyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String reply = replyField.getText();
                if (!reply.isEmpty()) {
                    postReply(currentCommentId, reply); // Post reply to the current comment
                    replyField.setText("");
                    loadReplies(currentCommentId, repliesPanel); // Reload replies
                }
            }
        });
        inputPanel.add(replyField, BorderLayout.CENTER);
        inputPanel.add(postReplyButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showRepliesPage(int commentId) {
        currentCommentId = commentId; // Set the current comment ID
        loadCommentWithReplies(commentId, repliesPanel);
        cardLayout.show(cardPanel, "Replies");
    }

    private void loadCommentWithReplies(int commentId, JPanel panel) {
        panel.removeAll();
        try {
            // Load the selected comment
            String commentSql = "SELECT comments.id, users.first_name, users.last_name, comments.comment, comments.timestamp, comments.user_id, "
                    + "(SELECT COUNT(*) FROM comment_likes WHERE comment_likes.comment_id = comments.id) AS likes, "
                    + "(SELECT COUNT(*) FROM comment_likes WHERE comment_likes.comment_id = comments.id AND comment_likes.user_id = ?) AS liked_by_user "
                    + "FROM comments "
                    + "JOIN users ON comments.user_id = users.id "
                    + "WHERE comments.id = ?";
            PreparedStatement commentStatement = connection.prepareStatement(commentSql);
            commentStatement.setInt(1, currentUserId);
            commentStatement.setInt(2, commentId);
            ResultSet commentResultSet = commentStatement.executeQuery();

            if (commentResultSet.next()) {
                String firstName = commentResultSet.getString("first_name");
                String lastName = commentResultSet.getString("last_name");
                String comment = commentResultSet.getString("comment");
                Timestamp timestamp = commentResultSet.getTimestamp("timestamp");
                int userId = commentResultSet.getInt("user_id");
                int likes = commentResultSet.getInt("likes");
                boolean likedByUser = commentResultSet.getInt("liked_by_user") > 0;

                JPanel commentPanel = new JPanel();
                commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
                commentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel nameLabel = new JLabel(firstName + " " + lastName + " - " + timestamp.toString());
                commentPanel.add(nameLabel);

                JTextArea commentArea = new JTextArea(comment);
                commentArea.setWrapStyleWord(true);
                commentArea.setLineWrap(true);
                commentArea.setEditable(false);
                commentArea.setBackground(Color.WHITE);

                int lineCount = commentArea.getLineCount();
                int preferredHeight = Math.min(100 + lineCount * 15, 200);
                commentArea.setPreferredSize(new Dimension(commentArea.getPreferredSize().width, preferredHeight));

                commentPanel.add(commentArea);

                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                JButton likeButton = new JButton(likedByUser ? "Unlike" : "Like");
                likeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (likedByUser) {
                            unlikeComment(commentId);
                        } else {
                            likeComment(commentId);
                        }
                        loadCommentWithReplies(commentId, panel);
                    }
                });

                JLabel likesLabel = new JLabel(likes + " likes");
                actionsPanel.add(likesLabel);
                actionsPanel.add(likeButton);

                if (userId == currentUserId) {
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int confirm = JOptionPane.showConfirmDialog(null,
                                    "Are you sure you want to delete this comment?", "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                deleteComment(commentId);
                                cardLayout.show(cardPanel, "Home");
                                loadComments(commentsPanel);
                            }
                        }
                    });
                    actionsPanel.add(deleteButton);
                }

                commentPanel.add(actionsPanel);

                panel.add(commentPanel);
                panel.add(Box.createVerticalStrut(10));

                // Load the replies for the selected comment
                loadReplies(commentId, panel);
            }

            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReplies(int commentId, JPanel panel) {
        try {
            String sql = "SELECT replies.id, users.first_name, users.last_name, replies.reply, replies.timestamp, replies.user_id, "
                    + "(SELECT COUNT(*) FROM reply_likes WHERE reply_likes.reply_id = replies.id) AS likes, "
                    + "(SELECT COUNT(*) FROM reply_likes WHERE reply_likes.reply_id = replies.id AND reply_likes.user_id = ?) AS liked_by_user "
                    + "FROM replies "
                    + "JOIN users ON replies.user_id = users.id "
                    + "WHERE replies.comment_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, currentUserId);
            statement.setInt(2, commentId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int replyId = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String reply = resultSet.getString("reply");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                int userId = resultSet.getInt("user_id");
                int likes = resultSet.getInt("likes");
                boolean likedByUser = resultSet.getInt("liked_by_user") > 0;

                JPanel replyPanel = new JPanel(new BorderLayout());
                replyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel nameLabel = new JLabel(firstName + " " + lastName + " - " + timestamp.toString());
                replyPanel.add(nameLabel, BorderLayout.NORTH);

                JTextArea replyArea = new JTextArea(reply);
                replyArea.setWrapStyleWord(true);
                replyArea.setLineWrap(true);
                replyArea.setEditable(false);
                replyArea.setBackground(Color.WHITE);

                replyPanel.add(replyArea, BorderLayout.CENTER);

                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                JButton likeButton = new JButton(likedByUser ? "Unlike" : "Like");
                likeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (likedByUser) {
                            unlikeReply(replyId);
                        } else {
                            likeReply(replyId);
                        }
                        loadReplies(commentId, panel);
                    }
                });

                JLabel likesLabel = new JLabel(likes + " likes");
                actionsPanel.add(likesLabel);
                actionsPanel.add(likeButton);

                if (userId == currentUserId) {
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int confirm = JOptionPane.showConfirmDialog(null,
                                    "Are you sure you want to delete this reply?", "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                deleteReply(replyId);
                                loadReplies(commentId, panel);
                            }
                        }
                    });
                    actionsPanel.add(deleteButton);
                }

                replyPanel.add(actionsPanel, BorderLayout.SOUTH);

                panel.add(replyPanel);
                panel.add(Box.createVerticalStrut(10));
            }

            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void postReply(int commentId, String reply) {
        try {
            String sql = "INSERT INTO replies (comment_id, user_id, reply) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.setInt(2, currentUserId);
            statement.setString(3, reply);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void likeReply(int replyId) {
        try {
            String sql = "INSERT INTO reply_likes (reply_id, user_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, replyId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void unlikeReply(int replyId) {
        try {
            String sql = "DELETE FROM reply_likes WHERE reply_id = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, replyId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteReply(int replyId) {
        try {
            String sql = "DELETE FROM replies WHERE id = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, replyId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteComment(int commentId) {
        try {
            String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void postComment(String comment) {
        try {
            String sql = "INSERT INTO comments (user_id, comment) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, currentUserId);
            statement.setString(2, comment);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void likeComment(int commentId) {
        try {
            String sql = "INSERT INTO comment_likes (comment_id, user_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void unlikeComment(int commentId) {
        try {
            String sql = "DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean registerUser(String firstName, String lastName, String email, String password) {
        String sql = "INSERT INTO users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, password);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    currentUserId = generatedKeys.getInt(1);
                    currentUserName = firstName + " " + lastName;
                }
                System.out.println("A new user was inserted successfully!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyLogin(String email, String password) {
        String sql = "SELECT id, first_name, last_name FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                currentUserId = resultSet.getInt("id");
                currentUserName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserApp();
            }
        });
    }
}
