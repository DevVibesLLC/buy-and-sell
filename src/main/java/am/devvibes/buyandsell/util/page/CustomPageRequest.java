package am.devvibes.buyandsell.util.page;

import am.devvibes.buyandsell.exception.PageRequestValidationException;
import am.devvibes.buyandsell.util.ExceptionConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static java.util.Objects.isNull;

public class CustomPageRequest extends PageRequest {

	private static final int PAGE_MIN_SIZE = 1;
	private static final int PAGE_MAX_SIZE = 1000;
	private static final int PAGE_DEFAULT_VALUE = 1;
	private static final int SIZE_DEFAULT_VALUE = 40;

	protected CustomPageRequest(int page, int size, Sort sort) {
		super(page, size, sort);
	}

	public static PageRequest from(Integer page, Integer size, Sort sort) {
		if (isNull(page)) {
			page = PAGE_DEFAULT_VALUE;
		}
		if (isNull(size)) {
			size = SIZE_DEFAULT_VALUE;
		}
		if (page < PAGE_MIN_SIZE) {
			throw new PageRequestValidationException(ExceptionConstants.PAGE_SIZE_EXCEPTION);
		}
		if (size < PAGE_MIN_SIZE || size > PAGE_MAX_SIZE) {
			throw new PageRequestValidationException(ExceptionConstants.PAGE_SIZE_EXCEPTION);
		}
		return new CustomPageRequest(page - 1, size, sort);
	}

}
