package com.techelevator.dao;


import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.User;
import exception.DaoException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.security.auth.login.AccountNotFoundException;
import javax.sql.DataSource;
import java.util.List;

public class JdbcUserDaoTests extends BaseDaoTests {

    private JdbcUserDao sut;
    private User USER_1;
    private User USER_2;

    @Before
    public void createUsers() {
        USER_1 = new User();
        USER_1.setUsername("bob");
        USER_1.setPassword("$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2");
        USER_1.setId(1001);
        USER_1.setAuthorities("USER");
        USER_1.setActivated(true);

        USER_2 = new User();
        USER_2.setUsername("user");
        USER_2.setPassword("$2a$10$Ud8gSvRS4G1MijNgxXWzcexeXlVs4kWDOkjE7JFIkNLKEuE57JAEy");
        USER_2.setId(1002);
        USER_2.setAuthorities("USER");
        USER_2.setActivated(true);
    }

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate, new JdbcAccountDao(jdbcTemplate));
    }

    @Test
    public void createNewUser() {
        boolean userCreated = sut.create("TEST_USER", "test_password");
        Assert.assertTrue(userCreated);
        User user = sut.findByUsername("TEST_USER");
        Assert.assertEquals("TEST_USER", user.getUsername());
    }


    @Test
    public void
    findIdByUsername_returns_correct_user_id() {
        int id1 = sut.findIdByUsername("bob");
        Assert.assertEquals(1001, id1);

        int id2 = sut.findIdByUsername("user");
        Assert.assertEquals(1002, id2);

        int badId = sut.findIdByUsername("who");
        Assert.assertEquals(-1, badId);
    }

    @Test
    public void
    findUsernameByAccountId_returns_correct_username() {
        String username1 = sut.findUsernameByAccountId(2001);
        Assert.assertEquals("bob", username1);

        String username2 = sut.findUsernameByAccountId(2002);
        Assert.assertEquals("user", username2);

        String nullUsername = sut.findUsernameByAccountId(2005);
        Assert.assertNull(nullUsername);
    }

    @Test
    public void
    findAll_returns_list_of_correct_size_and_username_values() {
        List<User> users = sut.findAll();

        Assert.assertEquals(2, users.size());

        assertUsersMatch(USER_1, users.get(0));
        assertUsersMatch(USER_2, users.get(1));
    }

    @Test
    public void findByUserName_returns_correct_user_information() {
        User user1 = sut.findByUsername("bob");
        assertUsersMatch(USER_1, user1);

        User user2 = sut.findByUsername("user");
        assertUsersMatch(USER_2, user2);

        try {
            sut.findByUsername("somethingthatdoesntexist");
            Assert.fail();
        } catch (UsernameNotFoundException e) {
            Assert.assertTrue(true);
        }
    }

    private void assertUsersMatch(User expected, User actual) {
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getAuthorities().toString(), actual.getAuthorities().toString());
        Assert.assertEquals(expected.getPassword(), actual.getPassword());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.isActivated(), actual.isActivated());
    }
}
