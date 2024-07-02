/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.avn.mobilita.anag.tracciato;

import it.mds.sdk.flusso.avn.mobilita.anag.parser.regole.RecordDtoAvnMobilitaAnag;
import it.mds.sdk.flusso.avn.mobilita.anag.parser.regole.conf.ConfigurazioneFlussoAvnMobilitaAnag;
import it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.InformazioniAnagrafiche;
import it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.Modalita;
import it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.ObjectFactory;
import it.mds.sdk.libreriaregole.tracciato.TracciatoSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component("tracciatoSplitterAvnMobAnag")
public class TracciatoSplitterImpl implements TracciatoSplitter<RecordDtoAvnMobilitaAnag> {

    @Override
    public List<Path> dividiTracciato(Path tracciato) {
        return null;
    }

    @Override
    public List<Path> dividiTracciato(List<RecordDtoAvnMobilitaAnag> records, String idRun) {
        try {
            ConfigurazioneFlussoAvnMobilitaAnag conf = getConfigurazione();

            //Imposto gli attribute element
            String modalita = records.get(0).getModalita();
            String codiceRegione = records.get(0).getCodRegione();

            //XML ANAGRAFICA
            it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.ObjectFactory objAnag = getObjectFactory();
            InformazioniAnagrafiche informazioniAnagrafiche = objAnag.createInformazioniAnagrafiche();
            //imposto la regione/periodo/anno che è unica per il file? TODO
            informazioniAnagrafiche.setCodiceRegione(codiceRegione);
            informazioniAnagrafiche.setModalita(it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.Modalita.fromValue(modalita));


            for (RecordDtoAvnMobilitaAnag r : records) {
                if (!r.getTipoTrasmissioneAnagrafica().equalsIgnoreCase("NM")) {
                    creaAnagraficaXml(r, informazioniAnagrafiche, objAnag);
                }
            }

            //recupero il path del file xsd di anagrafica
            URL resourceAnagraficaXsd = this.getClass().getClassLoader().getResource("AVM.xsd");
            log.debug("URL dell'XSD per la validazione idrun {} : {}", idRun, resourceAnagraficaXsd);


            //scrivi XML ANAGRAFICA
          //  GestoreFile gestoreFile = GestoreFileFactory.getGestoreFile("XML");

            String pathAnagraf = conf.getXmlOutput().getPercorso() + "SDK_AVT_AVM_" + records.get(0).getCampiInput().getPeriodoRiferimentoInput() + "_" + idRun + ".xml";
            //gestoreFile.scriviDto(informazioniAnagrafiche, pathAnagraf, resourceAnagraficaXsd);

            return List.of(Path.of(pathAnagraf));
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            log.error("[{}].dividiTracciato  - records[{}]  - idRun[{}] -" + e.getMessage(),
                    this.getClass().getName(),
                    e
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossibile validare il csv in ingresso. message: " + e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectFactory getObjectFactory() {
        return new it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.ObjectFactory();
    }

    public ConfigurazioneFlussoAvnMobilitaAnag getConfigurazione() {
        return new ConfigurazioneFlussoAvnMobilitaAnag();
    }

    private void creaAnagraficaXml(RecordDtoAvnMobilitaAnag r, InformazioniAnagrafiche informazioniAnagrafiche, it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.ObjectFactory objAnag) {

        //ASSISTITO
        InformazioniAnagrafiche.Assistito currentAssistito = creaAssistito(r, objAnag);
        informazioniAnagrafiche.getAssistito().add(currentAssistito);
    }

    private InformazioniAnagrafiche.Assistito creaAssistito(RecordDtoAvnMobilitaAnag r,
                                                            it.mds.sdk.flusso.avn.mobilita.anag.tracciato.bean.output.anagrafica.ObjectFactory objAnag) {

        InformazioniAnagrafiche.Assistito assistito = objAnag.createInformazioniAnagraficheAssistito();
        assistito.setTipoTrasmissione(r.getTipoTrasmissioneAnagrafica());
        assistito.setIdAssistito(r.getIdAssistito());
        assistito.setValiditaCI(BigInteger.valueOf(r.getValiditaCI()));
        assistito.setTipologiaCI(BigInteger.valueOf(r.getTipologiaCI()));
        assistito.setSesso(r.getSesso());
        XMLGregorianCalendar dataTrasferimentoResidenza = null;
        XMLGregorianCalendar dataNascita = null;
        XMLGregorianCalendar dataDecesso = null;
        try {
            dataTrasferimentoResidenza = r.getDataTrasferimentoResidenza() != null ? DatatypeFactory.newInstance().newXMLGregorianCalendar(r.getDataTrasferimentoResidenza()) : null;
            dataNascita = r.getDataNascita() != null ? DatatypeFactory.newInstance().newXMLGregorianCalendar(r.getDataNascita()) : null;
            dataDecesso = r.getDataDecesso() != null ? DatatypeFactory.newInstance().newXMLGregorianCalendar(r.getDataDecesso()) : null;
        } catch (DatatypeConfigurationException e) {
            log.debug(e.getMessage(), e);
        }
        assistito.setDataNascita(dataNascita);
        assistito.setComuneResidenza(r.getCodiceComuneResidenza());
        assistito.setAslResidenza(r.getCodiceAslResidenza());
        assistito.setRegioneResidenza(r.getCodiceRegioneResidenza());
        assistito.setStatoEsteroResidenza(r.getStatoEsteroResidenza());
        assistito.setDataTrasferimentoResidenza(dataTrasferimentoResidenza);
        assistito.setComuneDomicilio(r.getCodiceComuneDomicilio());
        assistito.setAslDomicilio(r.getCodiceAslDomicilio());
        assistito.setRegioneDomicilio(r.getCodiceRegioneDomicilio());
        assistito.setCittadinanza(r.getCittadinanza());
        assistito.setDataDecesso(dataDecesso);
        return assistito;
    }

    public InformazioniAnagrafiche creaInformazioniAnagrafiche(List<RecordDtoAvnMobilitaAnag> records, InformazioniAnagrafiche informazioniAnagrafiche) {

        //Imposto gli attribute element
        String modalita = records.get(0).getModalita();
        String codiceRegione = records.get(0).getCodRegione();

        if (informazioniAnagrafiche == null) {
            ObjectFactory objAnagInfAnag = getObjectFactory();
            informazioniAnagrafiche = objAnagInfAnag.createInformazioniAnagrafiche();
            informazioniAnagrafiche.setCodiceRegione(codiceRegione);
            informazioniAnagrafiche.setModalita(Modalita.fromValue(modalita));

            for (RecordDtoAvnMobilitaAnag r : records) {
                if (!r.getTipoTrasmissioneAnagrafica().equalsIgnoreCase("NM")) {
                    creaAnagraficaXml(r, informazioniAnagrafiche, objAnagInfAnag);
                }
            }

        }
        return informazioniAnagrafiche;
    }
}
