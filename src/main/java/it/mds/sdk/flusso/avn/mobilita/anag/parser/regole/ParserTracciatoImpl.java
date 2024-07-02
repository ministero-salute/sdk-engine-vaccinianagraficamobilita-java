/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.avn.mobilita.anag.parser.regole;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import it.mds.sdk.flusso.avn.mobilita.anag.parser.regole.conf.ConfigurazioneFlussoAvnMobilitaAnag;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.rest.exception.ParseCSVException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component("parserTracciatoAvnMobilitaAnag")
public class ParserTracciatoImpl implements ParserTracciato {

    private ConfigurazioneFlussoAvnMobilitaAnag conf = new ConfigurazioneFlussoAvnMobilitaAnag();

    /**
     * Il metodo converte un File.csv in una List<RecordDtoGenerico> usando come separatore "~"
     *
     * @param tracciato, File.csv di input
     * @return una lista di RecordDtoDir
     */
    @Override
    public List<RecordDtoGenerico> parseTracciato(File tracciato) {
        try {
            FileReader fileReader = getFileReader(tracciato);
            List<RecordDtoGenerico> dirList = getRecordList(fileReader);
            fileReader.close();

            return dirList;

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new ParseCSVException(ex.getMessage());
        }

        return Collections.emptyList();
    }

    public FileReader getFileReader(File tracciato) throws FileNotFoundException {
        System.out.println("lol");
        return new FileReader(tracciato);
    }
    public List<RecordDtoGenerico> getRecordList(FileReader fileReader) {
        char separatore = conf.getSeparatore().getSeparatore().charAt(0);
        return new CsvToBeanBuilder<RecordDtoGenerico>(fileReader)
                .withType(RecordDtoAvnMobilitaAnag.class)
                .withSeparator(separatore)
                .withSkipLines(1)   //Salta la prima riga del file CSV
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()
                .parse();
    }

    public List<RecordDtoGenerico> parseTracciatoBlocco(File tracciato, int inizio, int fine) {
        StopWatch stopWatch = new StopWatch();
        log.info("Inizio lettura file {} da riga {} a riga {}", tracciato.getName(), inizio, fine);
        stopWatch.start();
        try (Reader reader = Files.newBufferedReader(tracciato.toPath())) {
            List<RecordDtoGenerico> res = new ArrayList<>();
            Iterator<RecordDtoAvnMobilitaAnag> it = getIteratorFromReader(reader, inizio);
            int count = inizio;
            int totaleRecordElaborati = 0;
            while (it.hasNext() && count < fine) {
                count++;
                RecordDtoGenerico recordGen = it.next();
                res.add(recordGen);
                totaleRecordElaborati++;
            }
            stopWatch.stop();
            log.info("Fine lettura file {} da riga {} a riga {} con {} record in {} ms", tracciato.getName(), inizio,
                    fine, totaleRecordElaborati, stopWatch.getLastTaskTimeMillis());

            return res;
        } catch (IOException e) {
            throw new ParseCSVException(e.getMessage());
        } catch (Exception ex) {
            log.debug(ex.getMessage());
            throw new ParseCSVException(ex.getMessage());
        }
    }

    public Iterator<RecordDtoAvnMobilitaAnag> getIteratorFromReader(Reader reader, int inizio) {
        char separatore = conf.getSeparatore().getSeparatore().charAt(0);
        return new CsvToBeanBuilder<RecordDtoAvnMobilitaAnag>(reader)
                .withType(RecordDtoAvnMobilitaAnag.class)
                .withSeparator(separatore)
                .withSkipLines(inizio + 1)   //Salta la prima riga del file CSV
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()
                .iterator();
    }
}
