package TrabConstrSoft.TrabConstrSoft;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestAPIController {

	@RequestMapping(value = "tipoDados/cria")
	public String criaTipoDados(@RequestParam(value = "nome") String nome) throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();
		try {
			String[] json = { "{\"dados\": []}" };
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO DADOS (nome, dados) VALUES ('" + nome + "', to_json(?::json))");
			ps.setObject(1, json[0]);
			ps.executeQuery();
			return "Tipo de dado criado";
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "tipoDados/selecionaTodos")
	public String selecionaTodosDeUmTipo(@RequestParam(value = "nome") String nome)
			throws URISyntaxException, SQLException {
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT DADOS.dados->'dados' as dados FROM DADOS where nome=?");
			ps.setString(1, nome);
			ResultSet resultSet = ps.executeQuery();
			resultSet.next();
			JSONArray array = new JSONArray(resultSet.getString("dados"));
			return array.toString();
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "createTable")
	public String createTable() throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn
					.prepareStatement("CREATE TABLE DADOS (id SERIAL PRIMARY KEY, dados json, nome VARCHAR(40))");
			ps.executeQuery();
			return "Tabela criada";
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "tipoDados/insere", method = RequestMethod.POST)
	public String insere(@RequestBody String json, @RequestParam(value = "nome") String nome)
			throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();

		try {
			JSONArray array = new JSONArray(selecionaTodosDeUmTipo(nome));
			array.put(new JSONObject(json));
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("dados", array);
			PreparedStatement ps = conn.prepareStatement("UPDATE DADOS SET dados = json(?::json) where nome=?");
			ps.setString(1, jsonObj.toString());
			ps.setString(2, nome);
			ResultSet resultSet = ps.executeQuery();
			return ps.getResultSet().toString();
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "tipoDados/deletaTodos")
	public String deletaTodos(@RequestParam(value = "nome") String nome) throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();
		try {
			JSONArray array = new JSONArray();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("dados", array);
			PreparedStatement ps = conn.prepareStatement("UPDATE DADOS SET dados = json(?::json) where nome=?");
			ps.setString(1, jsonObj.toString());
			ps.setString(2, nome);
			ResultSet resultSet = ps.executeQuery();
			return ps.getResultSet().toString();
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "tipoDados/deleta/filtro", method = RequestMethod.POST)
	public String deletaFiltro(@RequestParam(value = "nome") String nome, @RequestBody String json)
			throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();

		try {
			return bugFixGambia(conn,false,nome,json);

		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	@RequestMapping(value = "tipoDados/seleciona/filtro", method = RequestMethod.POST)
	public String selecionaFiltro(@RequestParam(value = "nome") String nome, @RequestBody String json)
			throws SQLException, URISyntaxException {
		Connection conn = DBConnect.getConnection();

		try {
			return bugFixGambia(conn,true,nome,json);
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			conn.close();
		}
	}

	private String bugFixGambia(Connection conn, Boolean seleciona, String nome, String json) throws Exception {
		try {
			JSONArray arrayRetorno = new JSONArray();
			JSONArray array = new JSONArray(selecionaTodosDeUmTipo(nome));
			JSONObject filtro = new JSONObject(json);
			System.out.println("JSONARRAY " + array.toString());

			for (int i = 0; i < array.length(); i++) {
				System.out.println("JSONOBJECT " + array.getJSONObject(i).toString());

				JSONObject aux = array.getJSONObject(i);
				if (aux.has(filtro.getString("filtro"))) {
					if (aux.get(filtro.getString("filtro")).equals(filtro.get("valor"))) {
						if (seleciona) {
							arrayRetorno.put(aux);
						} else {
							array.remove(i);
						}
					}
				}
			}
			if (seleciona) {
				return arrayRetorno.toString();
			} else {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("dados", array);
				PreparedStatement ps = conn.prepareStatement("UPDATE DADOS SET dados = json(?::json) where nome=?");
				ps.setString(1, jsonObj.toString());
				ps.setString(2, nome);
				ResultSet resultSet = ps.executeQuery();
				return ps.getResultSet().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
