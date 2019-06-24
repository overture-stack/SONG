/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.model.enums;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class TableNames {

 public static final String STUDY = "Study";
 public static final String DONOR = "Donor";
 public static final String SPECIMEN = "Specimen";
 public static final String SAMPLE = "Sample";
 public static final String ANALYSIS = "Analysis";
 public static final String FILE = "File";
 public static final String SAMPLESET = "Sampleset";
 public static final String SEQUENCINGREAD = "Sequencingread";
 public static final String VARIANTCALL = "Variantcall";
 public static final String UPLOAD = "Upload";
 public static final String INFO = "Info";
 public static final String BUSINESS_KEY_VIEW = "Businesskeyview";
 public static final String ID_VIEW = "Idview";
 public static final String FULL_VIEW = "Fullview";

 public static final String SCHEMA = "schema";
}
