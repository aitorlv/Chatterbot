package com.example.aitor.conversacion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aitor.conversacion.libreria.ChatterBot;
import com.example.aitor.conversacion.libreria.ChatterBotFactory;
import com.example.aitor.conversacion.libreria.ChatterBotSession;
import com.example.aitor.conversacion.libreria.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private boolean sipuedo,robot=false;
    private static int CTE=0;
    private static int HABLA=1;
    private EditText tv;
    private TextView tvVelocidad,tvTono;
    private RadioGroup grupo;
    private String id1="",id2="";
    private TextToSpeech tts;
    private float tono=0;
    private float vel=2;
    private SeekBar barraVelocidad,barraTono;


    //tts.isSpeaking() este metodo comprueba si el tts esta hablando para que no pueda hacer nada mientras hablas
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grupo=(RadioGroup)findViewById(R.id.grupo);
        tv=(EditText)findViewById(R.id.etconv);
        tvVelocidad=(TextView)findViewById(R.id.tvVel);
        barraVelocidad=(SeekBar)findViewById(R.id.barvel);
        tvTono=(TextView)findViewById(R.id.tvtono);
        barraTono=(SeekBar)findViewById(R.id.barraTono);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE);
        iniciarBarras();
    }

    public void iniciarBarras(){
        tvVelocidad.setText( vel+ "");
        tvTono.setText(tono+"");
        barraVelocidad.setProgress(2);
        barraTono.setProgress(2);
        barraVelocidad.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //la Seekbar siempre empieza en cero, si queremos que el valor mínimo sea otro podemos modificarlo
                vel=progress ;
                tvVelocidad.setText( vel+ "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        barraTono.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //la Seekbar siempre empieza en cero, si queremos que el valor mínimo sea otro podemos modificarlo
                tono=progress ;
                tvTono.setText( tono+ "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == HABLA) {
            if (resultCode == RESULT_OK) {
                hebra h =new hebra();
                ArrayList <String> textos=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(!robot){
                    //tv.s(Color.RED);
                    tv.append("Yo: "+textos.get(0) + "\n");
                    robot=true;
                }
                if(grupo.getCheckedRadioButtonId()==R.id.rben) {
                    id1 = "en";
                    id2 = "US";

                }else{
                    id1 = "es";
                    id2 = "ES";
                }
                h.execute(textos.get(0));

            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            Log.w("entro","entro");
            tts.setLanguage(new Locale(id1, id2));
            tts.setPitch(tono);
            tts.setSpeechRate(vel);
            sipuedo=true;
        } else {
            sipuedo=false;
        }

    }

    public void hablar(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        startActivityForResult(i,HABLA);
    }


    class hebra extends AsyncTask<String ,String ,String>{
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tts.setLanguage(new Locale(id1, id2));
            tts.setPitch(tono);
            Log.v("velocidad hebra",vel+"");
            tts.setSpeechRate(vel);
            pd=new ProgressDialog(MainActivity.this);
            pd.setMessage("Esperando respuesta");
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();

        }


        @Override
        protected String doInBackground(String... params) {
            String palabra="";
            ChatterBotFactory factory = new ChatterBotFactory();
            ChatterBot bot1= null;
            try {
                bot1 = factory.create(ChatterBotType.CLEVERBOT);
                ChatterBotSession bot1session=bot1.createSession();
                palabra=bot1session.think(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return palabra;
        }
        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            pd.dismiss();
            if(sipuedo) {
                if(robot){
                    //tv.setTextColor(Color.BLUE);
                    /*String cadena="Robot: ";
                    SpannableStringBuilder ssb=new SpannableStringBuilder(cadena);
                    ForegroundColorSpan fcs=new ForegroundColorSpan(Color.rgb(255,255,255));
                    ssb.setSpan(fcs,0,cadena.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);*/
                    tv.append("Robot: "+strings+ "\n");
                    tts.speak(strings, TextToSpeech.QUEUE_ADD, null);
                    robot=false;
                }

            }
        }


    }

}
