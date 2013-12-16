/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.whispersystems.bithub.util;

import java.util.concurrent.atomic.AtomicLong;

public class AdvancedAtomicLong extends AtomicLong {

  public AdvancedAtomicLong(long initial) {
    super(initial);
  }

  public boolean setIfGreater(long compare, long update) {
    while(true) {
      long current = get();

      if (compare > current) {
        if (compareAndSet(current, update)) {
          return true;
        }
      } else {
        return false;
      }
    }
  }
}
