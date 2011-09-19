namespace :solr do
  task :solrindexcomments => :environment do
    builder = Builder::XmlMarkup.new()
      builder.add{
    Comment.find_each(:batch_size => 500) do |comment|

    end
    }
  end
end
