/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-net-path-space.
 *
 * ao-net-path-space is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-net-path-space is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-net-path-space.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.net.path_space;

import com.aoindustries.net.Path;
import com.aoindustries.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @see PathSpace
 *
 * @author  AO Industries, Inc.
 */
public class PathSpaceTest {

	private static PathSpace<Void> newTestSpace(String... prefixes) {
		// Add in a random order
		List<String> prefixList = new ArrayList<String>(Arrays.asList(prefixes));
		Collections.shuffle(prefixList);
		PathSpace<Void> testSpace = new PathSpace<Void>();
		for(String prefix : prefixList) {
			testSpace.put(Prefix.valueOf(prefix), null);
		}
		return testSpace;
	}

	@Test(expected = PrefixConflictException.class)
	public void testPrefixConflicts1() {
		newTestSpace(
			"/*",
			"/***"
		);
	}

	@Test(expected = PrefixConflictException.class)
	public void testPrefixConflicts2() {
		newTestSpace(
			"/*/*",
			"/path/other/*",
			"/other/path/*",
			"/path/*"
		);
	}

	private static <V> void assertEquals(PathSpace.PathMatch<V> expected, PathSpace.PathMatch<V> actual) {
		Assert.assertEquals("prefix", expected.getPrefix(), actual.getPrefix());
		Assert.assertEquals("prefixPath", expected.getPrefixPath(), actual.getPrefixPath());
		Assert.assertEquals("subPath", expected.getSubPath(), actual.getSubPath());
		Assert.assertEquals("value", expected.getValue(), actual.getValue());
	}

	private static <V> PathSpace.PathMatch<V> newPathMatch(String prefix, String prefixPath, String subPath, V value) throws ValidationException {
		return new PathSpace.PathMatch<V>(
			Prefix.valueOf(prefix),
			Path.valueOf(prefixPath),
			Path.valueOf(subPath),
			value
		);
	}

	@Test
	public void testGetWithRoot() throws ValidationException {
		PathSpace<Void> testSpace = newTestSpace(
			"/**",
			"/path/*",
			"/path/other/*",
			"/other/path/***"
		);
		assertEquals(
			newPathMatch("/**", "/", "/", (Void)null),
			testSpace.get(Path.ROOT)
		);
		assertEquals(
			newPathMatch("/**", "/", "/path", (Void)null),
			testSpace.get(Path.valueOf("/path"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/other", (Void)null),
			testSpace.get(Path.valueOf("/path/other"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/apple", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/pie", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/pie"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/pie/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/pie/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other", (Void)null),
			testSpace.get(Path.valueOf("/other"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other/", (Void)null),
			testSpace.get(Path.valueOf("/other/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other/path", (Void)null),
			testSpace.get(Path.valueOf("/other/path"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread/"))
		);
	}


	@Test
	public void testGetWithoutRoot() throws ValidationException {
		PathSpace<Void> testSpace = newTestSpace(
			"/path/*",
			"/path/other/*",
			"/other/path/***"
		);
		assertNull(
			testSpace.get(Path.ROOT)
		);
		assertNull(
			testSpace.get(Path.valueOf("/path"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/other", (Void)null),
			testSpace.get(Path.valueOf("/path/other"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/apple", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/pie"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/pie/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other/path"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread/"))
		);
	}
}
