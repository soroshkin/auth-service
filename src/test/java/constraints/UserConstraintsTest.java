package constraints;

import com.icl.auth.model.User;
import com.icl.auth.security.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserConstraintsTest {
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void checkConstraintsTest() {
        Set<ConstraintViolation<User>> violations = validator.validate(
                new User(null, null, null, null));
        assertThat(violations.size()).isEqualTo(6);

        violations = validator.validate(
                new User("", null, null, null));
        assertThat(violations.size()).isEqualTo(6);

        violations = validator.validate(new User("1234", null, null, null));
        assertThat(violations.size()).isEqualTo(4);

        violations = validator.validate(new User("1234", "", null, null));
        assertThat(violations.size()).isEqualTo(5);

        violations = validator.validate(new User("1234", "111", null, null));
        assertThat(violations.size()).isEqualTo(3);

        violations = validator.validate(new User("1234", "1w?", null, null));
        assertThat(violations.size()).isEqualTo(2);

        violations = validator.validate(
                new User("1234",
                        "1w?",
                        LocalDate.now().minus(5, ChronoUnit.DAYS),
                        null));
        assertThat(violations.size()).isEqualTo(1);

        violations = validator.validate(
                new User("1234",
                        "1w?",
                        LocalDate.now().minus(5, ChronoUnit.DAYS),
                        Role.USER));
        assertThat(violations.size()).isEqualTo(0);
    }
}
