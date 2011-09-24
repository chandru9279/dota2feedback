require "net/http"
require "builder/xmlmarkup"

class SolrUtil
  @@url= URI.parse("http://localhost:5000/solr/update")

  def self.update_solr(body)
    request = Net::HTTP::Post.new(@@url.path)
    request.content_type = "application/xml"
    request.body = body
    response = Net::HTTP.start(@@url.host, @@url.port) { |http| http.request(request) }
    puts response.to_s
    puts response.body
  end

  def self.commit_solr
    SolrUtil.update_solr("<commit/>")
  end

  def self.update_and_commit(body)
    SolrUtil.update_solr(body)
    SolrUtil.commit_solr
  end

  def self.index_comment(comment)
    builder = Builder::XmlMarkup.new()
    builder.add {
      builder.doc {
        builder.field(comment.id, :name => :"Id")
        builder.field(comment.text, :name => :"COMMENTS_OF_#{comment.commentable.id}")
      }
    }
    SolrUtil.update_solr(builder.target!)
    SolrUtil.commit_solr
  end

  def self.batch_index_comments(comments)
    builder = Builder::XmlMarkup.new()
    builder.add {
      comments.each do |comment|
        builder.doc {
          builder.field(comment.id, :name => :"Id")
          builder.field(comment.text, :name => :"COMMENTS_OF_#{comment.commentable.id}")
        }
      end
    }
    SolrUtil.update_solr(builder.target!)
    SolrUtil.commit_solr
  end

end