package l2r.gameserver.randoms;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BotCheckerManager
{
	private static final Logger _log = LoggerFactory.getLogger(BotCheckerManager.class);
	public static CopyOnWriteArrayList<BotCheckQuestion> _questions_data = new CopyOnWriteArrayList<BotCheckQuestion>();
	
	public static void load()
	{
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/bot_questions.xml");
		if (!file.exists())
		{
			_log.warn("BotCheckerManager: bot_questions.xml file is missing.");
			return;
		}
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			parseBotQuestions(doc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void parseBotQuestions(Document doc)
	{
		_questions_data.clear();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("question".equalsIgnoreCase(d.getNodeName()))
					{
						int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
						String question_en = d.getAttributes().getNamedItem("question_en").getNodeValue();
						String question_ru = d.getAttributes().getNamedItem("question_ru").getNodeValue();
						boolean answer = Integer.parseInt(d.getAttributes().getNamedItem("answer").getNodeValue()) == 0;
						
						BotCheckQuestion question_info = new BotCheckQuestion(id, question_en, question_ru, answer);
						_questions_data.add(question_info);
					}
				}
			}
		}
		_log.info("BotCheckerManager System: Loaded " + _questions_data.size() + " questions.");
	}
	
	public static class BotCheckQuestion
	{
		public final int _id;
		public final String _questionRus;
		public final String _questionEn;
		public final boolean _answer;
		
		public BotCheckQuestion(int id, String questionEn, String questionRus, boolean answer)
		{
			_id = id;
			_questionEn = questionEn;
			_questionRus = questionRus;
			_answer = answer;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getDescr(boolean rus)
		{
			if (rus)
				return _questionRus;
			
			return _questionEn;
		}
		
		public boolean getAnswer()
		{
			return _answer;
		}
	}
	
	public static CopyOnWriteArrayList<BotCheckQuestion> getAllAquisions()
	{
		return _questions_data;
	}
	
	public static boolean checkAnswer(int qId, boolean answer)
	{
		for (BotCheckQuestion info : _questions_data)
		{
			if (info._id == qId)
				return info.getAnswer() == answer;
		}
		return true;
	}
	
	public static BotCheckQuestion generateRandomQuestion()
	{
		return _questions_data.get(Rnd.get(0, _questions_data.size() - 1));
	}
}
