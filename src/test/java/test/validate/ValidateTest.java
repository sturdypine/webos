package test.validate;

import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import demo.DemoService;
import demo.ValidationParameter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/aop.xml")
public class ValidateTest {
	protected static Logger log = LoggerFactory.getLogger(ValidateTest.class);

	@Autowired
	DemoService demoService;

	@Test
	public void aop() throws Exception {
		OverridingMethodMustNotAlterParameterConstraints a;
		ExecutableMetaData b;
		try {
			 System.out.println(Object.class.isAssignableFrom(ValidationParameter.class));
			ValidationParameter vp = new ValidationParameter();
			vp.setName("123 456789");
			vp.setAge(19);
			System.out.println(demoService.sayHello("a s"));
//			System.out.println(POJOUtil.pojo2map(vp, null) + " \n\n\n");

//			demoService.save(vp);

		} catch (ConstraintViolationException ve) {
			Set<ConstraintViolation<?>> violations = ve.getConstraintViolations();
			log.info("err:{}", violations);
		}
		log.info("stop...");
	}

	public void val() throws Exception {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		ValidationParameter user = new ValidationParameter();
		user.setName("aa");
		user.setAge(1);
		Set<ConstraintViolation<ValidationParameter>> constraintViolations = validator.validate(user);
		System.out.println(constraintViolations.size());

		for (ConstraintViolation<ValidationParameter> constraintViolation : constraintViolations) {
			System.out.print(constraintViolation.getPropertyPath() + ": ");
			System.err.println(constraintViolation.getMessage());
		}
		// System.out.print(new Gson().toJson(constraintViolations));
		// new ConstraintViolationException("", constraintViolations);
	}

	public void se() throws Exception {
		ScriptEngineManager factory = new ScriptEngineManager();
		final ScriptEngine engine = factory.getEngineByName("js");
		final Invocable invocable = (Invocable) engine;
		String scriptText = "function add(a,b) { return a+b;} ";
		engine.eval(scriptText);
		for (int i = 0; i < 10; i++) {
			new Thread(() -> {
				try {
					for (int j = 0; j < 1000; j++) {
						Double d = (Double) invocable.invokeFunction("add", 1, 2);
						if (d != 3.0)
							System.out.println(d);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("thread  over");
			}).start();
		}
		Thread.sleep(100000);
	}
}
