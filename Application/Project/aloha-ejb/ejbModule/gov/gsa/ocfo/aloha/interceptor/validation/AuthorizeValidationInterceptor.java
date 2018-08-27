package gov.gsa.ocfo.aloha.interceptor.validation;

import gov.gsa.ocfo.aloha.exception.ValidationException;
import gov.gsa.ocfo.aloha.util.StringUtil;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class AuthorizeValidationInterceptor {
	
	private static final String ERROR_MSG = "Login Name is required.";
	@AroundInvoke
	public Object validate(final InvocationContext invocationContext) throws  Exception {
		String loginName = (String)invocationContext.getParameters()[0];
		if (StringUtil.isNullOrEmpty(loginName)) {
			throw new ValidationException(ERROR_MSG);
		}
		return invocationContext.proceed();
	}
}
